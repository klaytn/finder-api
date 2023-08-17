package io.klaytn.finder.service

import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.config.dynamic.FinderServerPaging
import io.klaytn.finder.domain.common.AccountAddress
import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.domain.mysql.set1.Contract
import io.klaytn.finder.domain.mysql.set3.token.*
import io.klaytn.finder.infra.cache.CacheName
import io.klaytn.finder.infra.cache.CacheUtils
import io.klaytn.finder.infra.client.opensearch.model.ContractSearchPageRequest
import io.klaytn.finder.infra.client.opensearch.model.ContractSearchRequest
import io.klaytn.finder.infra.utils.PageUtils
import io.klaytn.finder.infra.web.model.SimplePageRequest
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class TokenService(
    private val contractService: ContractService,
    private val tokenHolderRepository: TokenHolderRepository,
    private val tokenTransferRepository: TokenTransferRepository,
    private val tokenBurnRepository: TokenBurnRepository,
    private val tokenHolderCachedService: TokenHolderCachedService,
    private val tokenBurnCachedService: TokenBurnCachedService,
    private val tokenTransferCachedService: TokenTransferCachedService,
    private val finderServerPaging: FinderServerPaging
) {
    private val logger = logger(this::class.java)
    private val tokenContractType = ContractType.getTokenTypes()

    // -- --------------------------------------------------------------------------------------------------------------
    // -- token
    // -- --------------------------------------------------------------------------------------------------------------

    /**
     * Retrieve a list of verified tokens.
     */
    fun getVerifiedTokens(simplePageRequest: SimplePageRequest): Page<Contract> {
        val contractSearchRequest =
            ContractSearchRequest.ofPageForToken(
                null,
                true,
                null,
                null,
                ContractSearchPageRequest.of(simplePageRequest)
            )
        return contractService.search(contractSearchRequest)
    }

    fun getToken(contractAddress: String): Contract? {
        val contract = contractService.getContract(contractAddress)
        return if (tokenContractType.contains(contract?.contractType)) {
            contract
        } else
            null
    }

    // -- --------------------------------------------------------------------------------------------------------------
    // -- token holders
    // -- --------------------------------------------------------------------------------------------------------------

    /**
     * for token item view/ holders
     */
    fun getTokenHolders(contractAddress: String, holderAddress: String?, simplePageRequest: SimplePageRequest): Page<TokenHolder> {
        if(!holderAddress.isNullOrBlank()) {
            val contents = tokenHolderRepository.findByContractAddressAndHolderAddress(
                contractAddress,
                AccountAddress.of(holderAddress)
            )?.let { tokenHolderCachedService.getTokenHolders(listOf(it.id)) } ?: emptyList()
            return PageUtils.getPage(contents, simplePageRequest, contents.size.toLong())
        }

        val count = tokenHolderCachedService.countAllByContractAddress(
            contractAddress, finderServerPaging.limit.tokenHolder)
        PageUtils.checkPageParameter(simplePageRequest, count)

        val tokenHolders = tokenHolderRepository.findAllByContractAddress(
            contractAddress,
            simplePageRequest.pageRequest(Sort.by(Sort.Order.desc("amount"))))
        val contents = tokenHolders.map { it.id }.run { tokenHolderCachedService.getTokenHolders(this) }
        return PageUtils.getPage(contents, simplePageRequest, count)
    }

    /**
     * for account item view/ token balances
     */
    fun getTokenBalancesByHolder(
        holderAddress: String,
        simplePageRequest: SimplePageRequest,
    ): Page<TokenHolder> {
        val page = tokenHolderRepository.findAllByHolderAddress(
            AccountAddress.of(holderAddress),
            simplePageRequest.pageRequest(Sort.by(Sort.Order.desc("lastTransactionTime"))))
        val contents = page.content.map { it.id }.run { tokenHolderCachedService.getTokenHolders(this) }
        return PageUtils.getPage(contents, simplePageRequest, page.totalElements)
    }

    // -- --------------------------------------------------------------------------------------------------------------
    // -- token transfers
    // -- --------------------------------------------------------------------------------------------------------------

    /**
     * for transaction view/ token transfers
     */
    fun getTokenTransfersByTransactionHash(
        transactionHash: String,
        simplePageRequest: SimplePageRequest,
    ): Page<TokenTransfer> {
        val page = tokenTransferRepository.findAllByTransactionHash(
            transactionHash,
            simplePageRequest.pageRequest(Sort.by(Sort.Order.desc("displayOrder"))))
        val contents = page.content.map { it.id }.run { tokenTransferCachedService.getTokenTransfers(this) }
        return PageUtils.getPage(contents, simplePageRequest, page.totalElements)
    }

    /**
     * for account( eoa, sca) view/ token transfers
     */
    fun getTokenTransfersByAccountAddress(
        accountAddress: String,
        contractAddress: String? = null,
        blockNumberRange: LongRange? = null,
        simplePageRequest: SimplePageRequest,
    ): Page<TokenTransfer> {
        val maxLimit = finderServerPaging.limit.default

        val count =
            minOf(maxLimit,
                if(contractAddress != null) {
                    if(blockNumberRange == null) {
                        tokenTransferRepository.countAllByAccountAddressAndContractAddress(
                            accountAddress, contractAddress, maxLimit)
                    } else {
                        tokenTransferRepository.countAllByAccountAddressAndContractAddressAndBlockNumberBetween(
                            accountAddress, contractAddress, blockNumberRange.first, blockNumberRange.last, maxLimit)
                    }
                } else {
                    if(blockNumberRange == null) {
                        tokenTransferRepository.countAllByAccountAddress(accountAddress, maxLimit)
                    } else {
                        tokenTransferRepository.countAllByAccountAddressAndBlockNumberBetween(
                            accountAddress, blockNumberRange.first, blockNumberRange.last, maxLimit)
                    }

                }
            )
        PageUtils.checkPageParameter(simplePageRequest, count)

        val contents = getTokenTransfersByAccountAddressWithoutCounting(
            accountAddress, contractAddress, blockNumberRange, simplePageRequest, true)
        return PageUtils.getPage(contents, simplePageRequest, count)
    }

    fun getTokenTransfersByAccountAddressWithoutCounting(
        accountAddress: String,
        contractAddress: String? = null,
        blockNumberRange: LongRange? = null,
        simplePageRequest: SimplePageRequest,
        fillAccountAddress: Boolean
    ): List<TokenTransfer> {
        val limit = (simplePageRequest.page * simplePageRequest.size).toLong()
        val ids =
            if(contractAddress != null) {
                if(blockNumberRange == null) {
                    tokenTransferRepository.findAllByAccountAddressAndContractAddress(
                        accountAddress, contractAddress, limit, simplePageRequest.pageRequest())
                } else {
                    tokenTransferRepository.findAllByAccountAddressAndContractAddressAndBlockNumberBetween(
                        accountAddress, contractAddress,
                        blockNumberRange.first, blockNumberRange.last, limit,
                        simplePageRequest.pageRequest())
                }
            } else {
                if(blockNumberRange == null) {
                    tokenTransferRepository.findAllByAccountAddress(
                        accountAddress, limit, simplePageRequest.pageRequest())
                } else {
                    tokenTransferRepository.findAllByAccountAddressAndBlockNumberBetween(
                        accountAddress,
                        blockNumberRange.first, blockNumberRange.last, limit,
                        simplePageRequest.pageRequest())
                }
            }

        return ids.map { it.id }.run { tokenTransferCachedService.getTokenTransfers(this, fillAccountAddress) }
    }

    /**
     * for token view/ token transfers
     */
    fun getTokenTransfersByTokenAddress(
        contractAddress: String,
        blockNumberRange: LongRange? = null,
        simplePageRequest: SimplePageRequest,
    ): Page<TokenTransfer> {
        val maxLimit = finderServerPaging.limit.default
        val count =
            if(blockNumberRange == null) {
                tokenTransferRepository.countAllByContractAddress(contractAddress, maxLimit)
            } else {
                tokenTransferRepository.countAllByContractAddressAndBlockNumberBetween(
                    contractAddress, blockNumberRange.first, blockNumberRange.last, maxLimit)
            }

        PageUtils.checkPageParameter(simplePageRequest, count)

        val entityIds =
            if(blockNumberRange == null) {
                tokenTransferRepository.findAllByContractAddress(contractAddress, simplePageRequest.pageRequest())
            } else {
                tokenTransferRepository.findAllByContractAddressAndBlockNumberBetween(
                    contractAddress, blockNumberRange.first, blockNumberRange.last, simplePageRequest.pageRequest())
            }
        val contents = entityIds.map { it.id }.run { tokenTransferCachedService.getTokenTransfers(this) }
        return PageUtils.getPage(contents, simplePageRequest, count)
    }

    /**
     * List of burn events for a specific token.
     */
    fun getTokenBurnsByTokenAddress(
        contractAddress: String,
        blockNumberRange: LongRange? = null,
        simplePageRequest: SimplePageRequest,
    ): Page<TokenBurn> {
        val maxLimit = finderServerPaging.limit.default
        val count =
            if(blockNumberRange == null) {
                tokenBurnRepository.countAllByContractAddress(contractAddress, maxLimit)
            } else {
                tokenBurnRepository.countAllByContractAddressAndBlockNumberBetween(
                    contractAddress, blockNumberRange.first, blockNumberRange.last, maxLimit)
            }
        PageUtils.checkPageParameter(simplePageRequest, count)

        val entityIds =
            if(blockNumberRange == null) {
                tokenBurnRepository.findAllByContractAddress(contractAddress, simplePageRequest.pageRequest())
            } else {
                tokenBurnRepository.findAllByContractAddressAndBlockNumberBetween(
                    contractAddress, blockNumberRange.first, blockNumberRange.last, simplePageRequest.pageRequest())
            }
        val contents = entityIds.map { it.id }.run { tokenBurnCachedService.getTokenBurns(this) }
        return PageUtils.getPage(contents, simplePageRequest, count)
    }
}

@Service
class TokenHolderCachedService(
    private val accountAddressService: AccountAddressService,
    private val tokenHolderRepository: TokenHolderRepository,
    private val cacheUtils: CacheUtils,
) {
    @Cacheable(cacheNames = [CacheName.TOKEN_HOLDER_COUNT_BY_CONTRACT],
        key = "{#contractAddress, #maxTotalCount}", unless = "#result == null")
    fun countAllByContractAddress(contractAddress: String, maxTotalCount: Long) =
        tokenHolderRepository.countAllByContractAddress(contractAddress, maxTotalCount)

    fun getTokenHolders(searchIds: List<Long>): List<TokenHolder> {
        val tokenHolders =
            cacheUtils.getEntities(CacheName.TOKEN_HOLDER, TokenHolder::class.java, searchIds, tokenHolderRepository)
        accountAddressService.fillAccountAddress(tokenHolders.map { tokenHolder -> tokenHolder.holderAddress }.toList())
        return tokenHolders
    }
}

@Service
class TokenTransferCachedService(
    private val accountAddressService: AccountAddressService,
    private val tokenTransferRepository: TokenTransferRepository,
    private val cacheUtils: CacheUtils,
) {
    fun getTokenTransfers(searchIds: List<Long>) = getTokenTransfers(searchIds, true)

    fun getTokenTransfers(searchIds: List<Long>, fillAccountAddress: Boolean): List<TokenTransfer> {
        val tokenTransfers =
            cacheUtils.getEntities(CacheName.TOKEN_TRANSFER,
                TokenTransfer::class.java,
                searchIds,
                tokenTransferRepository)

        if(fillAccountAddress) {
            accountAddressService.fillAccountAddress(
                tokenTransfers.map { it.from }.toList(), tokenTransfers.mapNotNull { it.to }.toList())
        }
        return tokenTransfers
    }
}

@Service
class TokenBurnCachedService(
    private val accountAddressService: AccountAddressService,
    private val tokenBurnRepository: TokenBurnRepository,
    private val cacheUtils: CacheUtils,
) {
    fun getTokenBurns(searchIds: List<Long>): List<TokenBurn> {
        val tokenBurns =
            cacheUtils.getEntities(CacheName.TOKEN_BURN,
                TokenBurn::class.java,
                searchIds,
                tokenBurnRepository)
        accountAddressService.fillAccountAddress(
            tokenBurns.map { it.from }.toList(), tokenBurns.mapNotNull { it.to }.toList())
        return tokenBurns
    }
}
