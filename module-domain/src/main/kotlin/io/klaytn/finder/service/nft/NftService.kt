package io.klaytn.finder.service.nft

import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.config.dynamic.FinderServerPaging
import io.klaytn.finder.domain.common.AccountAddress
import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.domain.mysql.set1.Contract
import io.klaytn.finder.domain.mysql.set3.nft.*
import io.klaytn.finder.infra.cache.CacheName
import io.klaytn.finder.infra.cache.CacheUtils
import io.klaytn.finder.infra.client.opensearch.model.ContractSearchPageRequest
import io.klaytn.finder.infra.client.opensearch.model.ContractSearchRequest
import io.klaytn.finder.infra.exception.NotFoundNftException
import io.klaytn.finder.infra.utils.PageUtils
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.service.AccountAddressService
import io.klaytn.finder.service.ContractService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.math.BigInteger
import java.time.LocalDateTime

@Service
class NftService(
    private val contractService: ContractService,
    private val nft17Service: Nft17Service,
    private val nft37Service: Nft37Service,
    private val nftInventoryService: NftInventoryService,
    private val nftTransferRepository: NftTransferRepository,
    private val nftTransferCachedService: NftTransferCachedService,
    private val nftBurnRepository: NftBurnRepository,
    private val nftBurnCachedService: NftBurnCachedService,
    private val nftItemRepository: NftItemRepository,
    private val finderServerPaging: FinderServerPaging
) {
    private val logger = logger(this::class.java)
    private val nftContractType = ContractType.getNftTypes()

    // -- --------------------------------------------------------------------------------------------------------------
    // -- nft
    // -- --------------------------------------------------------------------------------------------------------------

    /**
     * Retrieve a list of verified NFTs.
     */
    fun getVerifiedNfts(simplePageRequest: SimplePageRequest): Page<Contract> {
        val contractSearchRequest =
            ContractSearchRequest.ofPageForNft(
                null,
                true,
                null,
                null,
                ContractSearchPageRequest.of(simplePageRequest)
            )
        return contractService.search(contractSearchRequest)
    }

    fun getNft(contractAddress: String): Contract? {
        val contract = contractService.getContract(contractAddress)
        return if (nftContractType.contains(contract?.contractType)) {
            contract
        } else
            null
    }

    fun getNftTokenItem(contractAddress: String, tokenId: String) =
        contractService.getContract(contractAddress)?.let {
            nftInventoryService.getNftTokenItem(it.contractType, contractAddress, tokenId)
        }

    /**
     * Replace getNftTokenItem based on the nft_item table.
     */
    fun getNftTokenItem2(contractAddress: String, tokenId: String) =
        nftItemRepository.findNftItemByContractAddressAndTokenId(contractAddress, tokenId)?.let { nftItem ->
            val holderAddress: AccountAddress? =
                if(nftItem.contractType == ContractType.KIP17) {
                    nftInventoryService.getNftInventory(nftItem.contractType, contractAddress, tokenId)?.holderAddress
                } else {
                    null
                }

            NftTokenItem(
                contractType = nftItem.contractType,
                contractAddress = nftItem.contractAddress,
                tokenId = nftItem.tokenId,
                tokenUri = nftItem.tokenUri,
                tokenUriUpdatedAt = nftItem.tokenUriUpdatedAt ?: nftItem.updatedAt!!,
                holderAddress = holderAddress,
                totalSupply = nftItem.totalSupply,
                totalTransfer = nftItem.totalTransfer,
                burnAmount = nftItem.burnAmount,
                totalBurn = nftItem.totalBurn,
            )
        }


    // -- --------------------------------------------------------------------------------------------------------------
    // -- nft transfers
    // -- --------------------------------------------------------------------------------------------------------------

    /**
     * for transaction view/ nft transfers
     */
    fun getNftTransfersByTransactionHash(
        transactionHash: String,
        simplePageRequest: SimplePageRequest,
    ): Page<NftTransfer> {
        val page = nftTransferRepository.findAllByTransactionHash(
            transactionHash,
            simplePageRequest.pageRequest(Sort.by(Sort.Order.desc("displayOrder"))))
        val contents = page.content.map { it.id }.run { nftTransferCachedService.getNftTransfers(this) }
        return PageUtils.getPage(contents, simplePageRequest, page.totalElements)
    }

    /**
     * for account( eoa, sca) view/ nft transfers
     */
    fun getNftTransfersByAccountAddress(
        accountAddress: String,
        contractAddress: String? = null,
        blockNumberRange: LongRange? = null,
        simplePageRequest: SimplePageRequest,
    ): Page<NftTransfer> {
        val maxLimit = finderServerPaging.limit.default
        val count =
            minOf(maxLimit,
                if(contractAddress != null) {
                    if(blockNumberRange == null) {
                        nftTransferRepository.countAllByAccountAddressAndContractAddress(
                            accountAddress, contractAddress, maxLimit)
                    } else {
                        nftTransferRepository.countAllByAccountAddressAndContractAddressAndBlockNumberBetween(
                            accountAddress, contractAddress, blockNumberRange.first, blockNumberRange.last, maxLimit)
                    }
                } else {
                    if(blockNumberRange == null) {
                        nftTransferRepository.countAllByAccountAddress(accountAddress, maxLimit)
                    } else {
                        nftTransferRepository.countAllByAccountAddressAndBlockNumberBetween(
                            accountAddress, blockNumberRange.first, blockNumberRange.last, maxLimit)
                    }

                }
            )
        PageUtils.checkPageParameter(simplePageRequest, count)

        val limit = (simplePageRequest.page * simplePageRequest.size).toLong()
        val ids =
            if(contractAddress != null) {
                if(blockNumberRange == null) {
                    nftTransferRepository.findAllByAccountAddressAndContractAddress(
                        accountAddress, contractAddress, limit, simplePageRequest.pageRequest())
                } else {
                    nftTransferRepository.findAllByAccountAddressAndContractAddressAndBlockNumberBetween(
                        accountAddress, contractAddress,
                        blockNumberRange.first, blockNumberRange.last,
                        limit, simplePageRequest.pageRequest())
                }
            } else {
                if(blockNumberRange == null) {
                    nftTransferRepository.findAllByAccountAddress(
                        accountAddress, limit, simplePageRequest.pageRequest())
                } else {
                    nftTransferRepository.findAllByAccountAddressAndBlockNumberBetween(
                        accountAddress, blockNumberRange.first, blockNumberRange.last,
                        limit, simplePageRequest.pageRequest())
                }

            }
        val contents = ids.map { it.id }.run { nftTransferCachedService.getNftTransfers(this) }
        return PageUtils.getPage(contents, simplePageRequest, count)
    }

    /**
     * for nft view/ nft transfers
     */
    fun getNftTransfersByNftAddressAndTokenId(
        contractAddress: String,
        blockNumberRange: LongRange? = null,
        tokenId: String?,
        simplePageRequest: SimplePageRequest,
    ): Page<NftTransfer> {
        val maxLimit = finderServerPaging.limit.default
        val count =
            if (tokenId.isNullOrBlank()) {
                if(blockNumberRange == null) {
                    nftTransferRepository.countAllByContractAddress(contractAddress, maxLimit)
                } else {
                    nftTransferRepository.countAllByContractAddressAndBlockNumberBetween(
                        contractAddress, blockNumberRange.first, blockNumberRange.last, maxLimit)
                }
            } else {
                nftTransferRepository.countAllByContractAddressAndTokenId(contractAddress,
                    tokenId,
                    maxLimit)
            }
        PageUtils.checkPageParameter(simplePageRequest, count)

        val entityIds =
            if (tokenId.isNullOrBlank()) {
                if(blockNumberRange == null) {
                    nftTransferRepository.findAllByContractAddress(contractAddress, simplePageRequest.pageRequest())
                } else {
                    nftTransferRepository.findAllByContractAddressAndBlockNumberBetween(
                        contractAddress, blockNumberRange.first, blockNumberRange.last, simplePageRequest.pageRequest())
                }
            } else {
                nftTransferRepository.findAllByContractAddressAndTokenId(contractAddress,
                    tokenId,
                    simplePageRequest.pageRequest())
            }
        val contents = entityIds.map { it.id }.run { nftTransferCachedService.getNftTransfers(this) }
        return PageUtils.getPage(contents, simplePageRequest, count)
    }

    // -- --------------------------------------------------------------------------------------------------------------
    // -- nft balances
    // -- --------------------------------------------------------------------------------------------------------------

    fun getNftBalancesOfHolder(
        contractType: ContractType,
        holderAddress: String,
        simplePageRequest: SimplePageRequest,
    ): Page<NftHolder> {
        return when (contractType) {
            ContractType.KIP17 -> nft17Service.getNftBalancesByHolder(holderAddress, simplePageRequest)
            ContractType.KIP37 -> nft37Service.getNftBalancesByHolder(holderAddress, simplePageRequest)
            else -> PageUtils.getPage(emptyList(), simplePageRequest, 0)
        }
    }

    // -- --------------------------------------------------------------------------------------------------------------
    // -- nft holders
    // -- --------------------------------------------------------------------------------------------------------------

    /**
     * for nft item view/ holders
     */
    fun getNftHoldersByNftAddressAndTokenId(
        contractAddress: String,
        tokenId: String?,
        simplePageRequest: SimplePageRequest,
    ): Page<NftHolder> {
        val contract = contractService.getContract(contractAddress) ?: throw NotFoundNftException()
        return when (contract.contractType) {
            ContractType.KIP17, ContractType.ERC721 -> nft17Service.getNftHolders(contractAddress, simplePageRequest)
            ContractType.KIP37, ContractType.ERC1155 -> nft37Service.getNftHolders(contractAddress, tokenId, simplePageRequest)
            else -> PageUtils.getPage(emptyList(), simplePageRequest, 0)
        }
    }

    // -- --------------------------------------------------------------------------------------------------------------
    // -- nft inventories, burns
    // -- --------------------------------------------------------------------------------------------------------------

    fun getNftInventories(
        contractAddress: String,
        keyword: String?,
        simplePageRequest: SimplePageRequest,
    ) =
        nftInventoryService.getNftInventoriesByContractAddressOrderByLatest(contractAddress, keyword, simplePageRequest)

    fun getBurnsByNftAddressAndTokenId(
        contractAddress: String,
        tokenId: String?,
        simplePageRequest: SimplePageRequest,
    ): Page<NftBurn> {
        val count =
            if(tokenId.isNullOrBlank()) {
                nftBurnRepository.countAllByContractAddress(contractAddress, finderServerPaging.limit.default)
            } else {
                nftBurnRepository.countAllByContractAddressAndTokenId(contractAddress, tokenId, finderServerPaging.limit.default)
            }
        PageUtils.checkPageParameter(simplePageRequest, count)

        val entityIds =
            if (tokenId.isNullOrBlank()) {
                nftBurnRepository.findAllByContractAddress(contractAddress, simplePageRequest.pageRequest())
            } else {
                nftBurnRepository.findAllByContractAddressAndTokenId(contractAddress,
                    tokenId,
                    simplePageRequest.pageRequest())
            }
        val contents = entityIds.map { it.id }.run { nftBurnCachedService.getNftBurns(this) }
        return PageUtils.getPage(contents, simplePageRequest, count)
    }
}

data class NftHolder(
    val contractType: ContractType,

    // nft17, nft37
    val contractAddress: String,
    val holderAddress: AccountAddress,
    val tokenCount: BigInteger,
    val lastTransactionTime: Int,

    // nft37
    val tokenId: String? = null,
    val tokenUri: String? = null,
)

data class NftTokenItem(
    val contractType: ContractType,

    // nft17, nft37
    val contractAddress: String,
    val tokenId: String,
    val tokenUri: String,
    val tokenUriUpdatedAt: LocalDateTime,

    // nft17
    var holderAddress: AccountAddress? = null,

    // nft37
    val totalSupply: BigInteger? = null,
    val totalTransfer: Long? = null,
    val burnAmount: BigInteger? = null,
    val totalBurn: Long? = null,
)

@Service
class NftTransferCachedService(
    private val accountAddressService: AccountAddressService,
    private val nftTransferRepository: NftTransferRepository,
    private val cacheUtils: CacheUtils,
) {
    fun getNftTransfers(searchIds: List<Long>): List<NftTransfer> {
        val nftTransfers =
            cacheUtils.getEntities(CacheName.NFT_TRANSFER, NftTransfer::class.java, searchIds, nftTransferRepository)
        accountAddressService.fillAccountAddress(
            nftTransfers.map { it.from }.toList(), nftTransfers.mapNotNull { it.to }.toList())
        return nftTransfers
    }
}

@Service
class NftBurnCachedService(
    private val accountAddressService: AccountAddressService,
    private val nftBurnRepository: NftBurnRepository,
    private val cacheUtils: CacheUtils,
) {
    fun getNftBurns(searchIds: List<Long>): List<NftBurn> {
        val nftTransfers =
            cacheUtils.getEntities(CacheName.NFT_BURN, NftBurn::class.java, searchIds, nftBurnRepository)
        accountAddressService.fillAccountAddress(
            nftTransfers.map { it.from }.toList(), nftTransfers.mapNotNull { it.to }.toList())
        return nftTransfers
    }
}
