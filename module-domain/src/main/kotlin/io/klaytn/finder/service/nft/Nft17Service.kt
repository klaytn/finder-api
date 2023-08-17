package io.klaytn.finder.service.nft

import io.klaytn.finder.config.dynamic.FinderServerPaging
import io.klaytn.finder.domain.common.AccountAddress
import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.domain.mysql.set3.nft.Nft17Holder
import io.klaytn.finder.domain.mysql.set3.nft.Nft17HolderRepository
import io.klaytn.finder.infra.cache.CacheName
import io.klaytn.finder.infra.cache.CacheUtils
import io.klaytn.finder.infra.utils.PageUtils
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.service.AccountAddressService
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class Nft17Service(
    private val nft17HolderRepository: Nft17HolderRepository,
    private val nft17HolderCachedService: Nft17HolderCachedService,
    private val finderServerPaging: FinderServerPaging
) {
    private val contractType = ContractType.KIP17

    // -- --------------------------------------------------------------------------------------------------------------
    // -- nft balances
    // -- --------------------------------------------------------------------------------------------------------------

    /**
     * for account item view/ nft xx balances
     */
    fun getNftBalancesByHolder(
        holderAddress: String,
        simplePageRequest: SimplePageRequest,
    ): Page<NftHolder> {
        val page = nft17HolderRepository.findAllByHolderAddress(
            AccountAddress.of(holderAddress),
            simplePageRequest.pageRequest(Sort.by(Sort.Order.desc("lastTransactionTime"))))
        val contents = page.content
            .map { it.id }
            .run { nft17HolderCachedService.getNftHolders(this) }
            .map {
                NftHolder(
                    contractType = contractType,
                    contractAddress = it.contractAddress,
                    holderAddress = it.holderAddress,
                    tokenCount = it.tokenCount,
                    lastTransactionTime = it.lastTransactionTime
                )
            }
        return PageUtils.getPage(contents, simplePageRequest, page.totalElements)
    }

    // -- --------------------------------------------------------------------------------------------------------------
    // -- nft holders
    // -- --------------------------------------------------------------------------------------------------------------

    fun getNftHolders(contractAddress: String, simplePageRequest: SimplePageRequest): Page<NftHolder> {
        val count = nft17HolderCachedService.countAllByContractAddress(
            contractAddress, finderServerPaging.limit.nft17Holder)
        PageUtils.checkPageParameter(simplePageRequest, count)

        val nftHolders = nft17HolderRepository.findAllByContractAddress(
            contractAddress,
            simplePageRequest.pageRequest(Sort.by(Sort.Order.desc("tokenCount"))))
        val contents = nftHolders
            .map { it.id }
            .run { nft17HolderCachedService.getNftHolders(this) }
            .map {
                NftHolder(
                    contractType = contractType,
                    contractAddress = it.contractAddress,
                    holderAddress = it.holderAddress,
                    tokenCount = it.tokenCount,
                    lastTransactionTime = it.lastTransactionTime
                )
            }
        return PageUtils.getPage(contents, simplePageRequest, count)
    }
}

@Service
class Nft17HolderCachedService(
    private val accountAddressService: AccountAddressService,
    private val nft17HolderRepository: Nft17HolderRepository,
    private val cacheUtils: CacheUtils,
) {
    @Cacheable(cacheNames = [CacheName.NFT_17_HOLDER_COUNT_BY_CONTRACT],
        key = "{#contractAddress, #maxTotalCount}", unless = "#result == null")
    fun countAllByContractAddress(contractAddress: String, maxTotalCount: Long) =
        nft17HolderRepository.countAllByContractAddress(contractAddress, maxTotalCount)

    fun getNftHolders(searchIds: List<Long>): List<Nft17Holder> {
        val nft17Holders =
            cacheUtils.getEntities(CacheName.NFT_17_HOLDER, Nft17Holder::class.java, searchIds, nft17HolderRepository)
        accountAddressService.fillAccountAddress(nft17Holders.map { nftHolder -> nftHolder.holderAddress }.toList())
        return nft17Holders
    }
}