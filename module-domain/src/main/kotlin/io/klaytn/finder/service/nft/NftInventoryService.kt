package io.klaytn.finder.service.nft

import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.config.dynamic.FinderServerPaging
import io.klaytn.finder.domain.common.AccountAddress
import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.domain.mysql.set1.Contract
import io.klaytn.finder.domain.mysql.set3.nft.NftInventory
import io.klaytn.finder.domain.mysql.set3.nft.NftInventoryRepository
import io.klaytn.finder.infra.cache.CacheName
import io.klaytn.finder.infra.cache.CacheUtils
import io.klaytn.finder.infra.db.DbConstants
import io.klaytn.finder.infra.utils.PageUtils
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.service.AccountAddressService
import io.klaytn.finder.service.caver.CaverAccountService
import io.klaytn.finder.service.caver.CaverContractService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@Service
class NftInventoryService(
    private val caverAccountService: CaverAccountService,
    private val caverContractService: CaverContractService,
    private val nftInventoryRepository: NftInventoryRepository,
    private val nftInventoryCachedService: NftInventoryCachedService,
    private val finderServerPaging: FinderServerPaging,
    private val nftInventoryRefreshRequestService: NftInventoryRefreshRequestService,
) {
    private val logger = logger(this::class.java)

    fun getNftInventoriesByContractAddressOrderByLatest(
        contractAddress: String,
        keyword: String?,
        simplePageRequest: SimplePageRequest,
    ) =
        getNftInventoriesByContractAddress(
            contractAddress, keyword, simplePageRequest, NftInventorySortType.LAST_TRANSACTION_TIME
        )

    fun getNftInventoriesByContractAddressOrderByTokenCount(
        contractAddress: String,
        keyword: String?,
        simplePageRequest: SimplePageRequest,
    ) =
        getNftInventoriesByContractAddress(
            contractAddress, keyword, simplePageRequest, NftInventorySortType.TOKEN_COUNT
        )

    fun getNftInventoriesByHolderAddress(
        holderAddress: String,
        excludeIfTokenUriIsEmpty: Boolean,
        simplePageRequest: SimplePageRequest,
    ): Page<NftInventory> {
        val pageRequest = simplePageRequest.pageRequest(NftInventorySortType.LAST_TRANSACTION_TIME.sort)
        val page =
            if(excludeIfTokenUriIsEmpty) {
                nftInventoryRepository.findAllByHolderAddressAndTokenUriIsNot(
                    AccountAddress.of(holderAddress), "-", pageRequest)
            } else {
                nftInventoryRepository.findAllByHolderAddress(AccountAddress.of(holderAddress), pageRequest)
            }

        val contents = page.content
            .map { it.id }
            .run { nftInventoryCachedService.getNftInventories(this) }
            .toList()
        return PageUtils.getPage(contents, simplePageRequest, page.totalElements)
    }

    fun getNft37InventoriesByHolderAddress(
        holderAddress: String,
        simplePageRequest: SimplePageRequest,
    ): Page<NftInventory> {
        val pageRequest = simplePageRequest.pageRequest(NftInventorySortType.LAST_TRANSACTION_TIME.sort)
        val page = nftInventoryRepository.findAllByContractTypeAndHolderAddress(
            ContractType.KIP37, AccountAddress.of(holderAddress), pageRequest)

        val contents = page.content
            .map { it.id }
            .run { nftInventoryCachedService.getNftInventories(this) }
            .toList()
        return PageUtils.getPage(contents, simplePageRequest, page.totalElements)
    }

    fun getNftTokenItem(contractType: ContractType, contractAddress: String, tokenId: String) =
        getNftInventory(contractType, contractAddress, tokenId)?.let { nftInventory ->
                NftTokenItem(
                    contractType = contractType,
                    contractAddress = contractAddress,
                    tokenId = tokenId,
                    tokenUri = nftInventory.tokenUri,
                    tokenUriUpdatedAt = nftInventory.updatedAt!!,
                    holderAddress = nftInventory.holderAddress
                )
            }

    fun getNftInventory(contractType: ContractType, contractAddress: String, tokenId: String) =
        nftInventoryRepository.findFirstByContractAddressAndTokenId(contractAddress, tokenId)?.let {
            nftInventoryCachedService.getNftInventories(listOf(it.id)).firstOrNull()
        }

    private fun getNftInventoriesByContractAddress(
        contractAddress: String,
        keyword: String?,
        simplePageRequest: SimplePageRequest,
        nftInventorySortType: NftInventorySortType,
    ): Page<NftInventory> {
        val page =
            if (keyword.isNullOrBlank()) {
                val count = nftInventoryCachedService.countAllByContractAddress(
                    contractAddress, finderServerPaging.limit.nftInventory)
                PageUtils.checkPageParameter(simplePageRequest, count)

                val pageRequest = simplePageRequest.pageRequest()
                val nftInventories =
                    if(nftInventorySortType == NftInventorySortType.LAST_TRANSACTION_TIME) {
                        nftInventoryRepository.findAllByContractAddressOrderByLastTransactionTime(contractAddress, pageRequest)
                    } else {
                        nftInventoryRepository.findAllByContractAddressOrderByTokenCount(contractAddress, pageRequest)
                    }
                PageUtils.getPage(nftInventories, simplePageRequest, count)
            } else {
                val pageRequest = simplePageRequest.pageRequest(nftInventorySortType.sort)
                if (caverAccountService.isAccountAddress(keyword)) {
                    nftInventoryRepository.findAllByContractAddressAndHolderAddress(
                        contractAddress, AccountAddress.of(keyword), pageRequest)
                } else {
                    nftInventoryRepository.findAllByContractAddressAndTokenId(
                        contractAddress, keyword, pageRequest)
                }
            }

        val contents = page.content
            .map { it.id }
            .run { nftInventoryCachedService.getNftInventories(this) }
            .toList()
        return PageUtils.getPage(contents, simplePageRequest, page.totalElements)
    }

    // -- --------------------------------------------------------------------------------------------------------------
    // -- update nft token uri 
    // -- --------------------------------------------------------------------------------------------------------------

    fun getNftTokenUri(contract: Contract, tokenId: String, defaultIfNull: String? = "-") =
         caverContractService.getTokenUri(contract.contractType, contract.contractAddress, tokenId) ?: defaultIfNull

    @Transactional(DbConstants.set3TransactionManager)
    fun refreshNftTokenUri(contractAddress: String, tokenId: String, tokenUri: String?, batchSize: Int = 100): Boolean {
        try {
            if(tokenUri == null) {
                return false
            }

            var currentPage = 1
            while(true) {
                val simplePageRequest = SimplePageRequest(page = currentPage, size = batchSize)
                val page = nftInventoryRepository.findAllByContractAddressAndTokenId(
                    contractAddress, tokenId, simplePageRequest.pageRequest())

                if(page.hasContent()) {
                    val ids = page.content.map { it.id }
                    nftInventoryRepository.updateTokenUri(ids, tokenId, tokenUri)
                    registerAfterCommitSynchronization(ids)
                }

                if(page.isLast) {
                    break
                } else {
                    currentPage += 1
                }
            }
            return true
        } finally {
            nftInventoryRefreshRequestService.deleteRefreshNftTokenUriLimiter(contractAddress, tokenId)
        }
    }

    private fun registerAfterCommitSynchronization(ids: List<Long>) {
        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    ids.map { nftInventoryCachedService.flush(it) }
                }
            })
    }
}

@Service
class NftInventoryCachedService(
    private val accountAddressService: AccountAddressService,
    private val nftInventoryRepository: NftInventoryRepository,
    private val cacheUtils: CacheUtils,
) {
    @Cacheable(cacheNames = [CacheName.NFT_INVENTORY_COUNT_BY_CONTRACT],
        key = "{#contractAddress, #maxTotalCount}", unless = "#result == null")
    fun countAllByContractAddress(contractAddress: String, maxTotalCount: Long) =
        nftInventoryRepository.countAllByContractAddress(contractAddress, maxTotalCount)

    fun getNftInventories(searchIds: List<Long>): List<NftInventory> {
        val nftInventories =
            cacheUtils.getEntities(CacheName.NFT_INVENTORY,
                NftInventory::class.java,
                searchIds,
                nftInventoryRepository)
        accountAddressService.fillAccountAddress(nftInventories.map { nftInventory -> nftInventory.holderAddress }
            .toList())
        return nftInventories
    }

    @CacheEvict(cacheNames = [CacheName.NFT_INVENTORY], key = "#id")
    fun flush(id: Long) {
    }
}

enum class NftInventorySortType(val sort: Sort) {
    LAST_TRANSACTION_TIME(Sort.by(Sort.Order.desc("lastTransactionTime"))),
    TOKEN_COUNT(Sort.by(Sort.Order.desc("tokenCount"))),
    ;
}