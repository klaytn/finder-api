package io.klaytn.finder.service

import com.klaytn.caver.transaction.type.TransactionType
import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.config.ChainProperties
import io.klaytn.finder.config.dynamic.FinderServerPaging
import io.klaytn.finder.domain.mysql.set1.*
import io.klaytn.finder.infra.cache.CacheName
import io.klaytn.finder.infra.cache.CacheUtils
import io.klaytn.finder.infra.client.opensearch.TransactionSearchClient
import io.klaytn.finder.infra.client.opensearch.model.TransactionSearchPageRequest
import io.klaytn.finder.infra.client.opensearch.model.TransactionSearchRequest
import io.klaytn.finder.infra.client.opensearch.model.TransactionSearchRequestByAccountAddress
import io.klaytn.finder.infra.utils.PageUtils
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.service.accountkey.AccountKeyService
import io.klaytn.finder.service.accountkey.KlaytnAccountKeyService
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.math.BigDecimal
import org.springframework.data.jpa.domain.Specification
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

@Service
class TransactionService(
    private val transactionCachedService: TransactionCachedService,
    private val transactionSearchClient: TransactionSearchClient,
    private val gasPriceService: GasPriceService,
    private val chainProperties: ChainProperties,
    private val accountKeyService: AccountKeyService,
) {
    private val logger = logger(this::class.java)

    fun isTransactionAddress(address: String) =
        address.startsWith("0x") && address.length == (64 + 2)

    fun getMaxTransactionId() = transactionCachedService.getMaxTransactionId()

    fun getTransactions(
        blockRange: LongRange?,
        transactionType: TransactionType?,
        simplePageRequest: SimplePageRequest
    ): Page<Transaction> {
        val count =
            if (blockRange == null) {
                transactionCachedService.countByTransactionType(transactionType)
            } else {
                transactionCachedService.countByBlockRangeAndTransactionType(blockRange, transactionType)
            }
        PageUtils.checkPageParameter(simplePageRequest, count)

        val entityIds =
            if (blockRange == null) {
                transactionCachedService.getTransactionHashesByTransactionType(transactionType, simplePageRequest)
            } else {
                transactionCachedService.getTransactionHashesByBlockRangeAndTransactionType(
                    blockRange, transactionType, simplePageRequest
                )
            }
        val contents = entityIds.run { transactionCachedService.getTransactions(this) }
        return PageUtils.getPage(contents, simplePageRequest, count)
    }

    fun getTransactionsByBlockNumberAndType(
        blockNumber: Long,
        transactionType: TransactionType?,
        simplePageRequest: SimplePageRequest,
    ): Page<Transaction> {
        val page = transactionCachedService.getTransactionHashesByBlockNumberAndTransactionType(
            blockNumber,
            transactionType,
            simplePageRequest
        )
        val contents = page.content.map { it.transactionHash }.run { transactionCachedService.getTransactions(this) }
        return PageUtils.getPage(contents, simplePageRequest, page.totalElements)
    }

    fun getTransactionsByAccountAddress(
        accountAddress: String,
        blockNumberRange: LongRange? = null,
        transactionType: TransactionType?,
        simplePageRequest: SimplePageRequest,
    ): Page<Transaction> {
        val count = transactionCachedService.countByAccountAddressAndTransactionType(
            accountAddress, blockNumberRange, transactionType
        )
        PageUtils.checkPageParameter(simplePageRequest, count)

        val contents = getTransactionsByAccountAddressWithoutCounting(
            accountAddress, blockNumberRange, transactionType, simplePageRequest, true
        )
        return PageUtils.getPage(contents, simplePageRequest, count)
    }

    fun getTransactionsByAccountAddressWithoutCounting(
        accountAddress: String,
        blockNumberRange: LongRange? = null,
        transactionType: TransactionType?,
        simplePageRequest: SimplePageRequest,
        fillAccountAddress: Boolean
    ): List<Transaction> {
        return transactionCachedService.getTransactionHashesByAccountAddressAndTransactionType(
            accountAddress, blockNumberRange, transactionType, simplePageRequest
        )
            .run { transactionCachedService.getTransactions(this, fillAccountAddress) }
    }

    fun getTransactionsByFeePayer(
        feePayer: String,
        blockNumberRange: LongRange? = null,
        transactionType: TransactionType?,
        simplePageRequest: SimplePageRequest,
    ): Page<Transaction> {
        val count = transactionCachedService.countByFeePayerAndTransactionType(
            feePayer, blockNumberRange, transactionType
        )
        PageUtils.checkPageParameter(simplePageRequest, count)

        val contents = transactionCachedService.getTransactionHashesByFeePayerAndTransactionType(
            feePayer, blockNumberRange, transactionType, simplePageRequest
        )
            .run { transactionCachedService.getTransactions(this) }
        return PageUtils.getPage(contents, simplePageRequest, count)
    }

    fun getTransactionByHash(transactionHash: String) =
        transactionCachedService.getTransaction(transactionHash)

    fun getTransactionByHashes(transactionHashes: List<String>) =
        transactionCachedService.getTransactions(transactionHashes)

    fun getTransactionByBlockNumberAndTransactionIndices(blockNumber: Long, transactionIndices: List<Int>) =
        transactionCachedService.getTransactionHashesByBlockNumberAndTransactionIndices(blockNumber, transactionIndices)
            .map { it.transactionHash }.run { transactionCachedService.getTransactions(this) }

    fun getTransactionByBlockNumbersAndTransactionIndices(blockIndexPairs: List<Pair<Long, Int>>) =
    transactionCachedService.getTransactionByBlockNumbersAndTransactionIndices(blockIndexPairs)

    fun getTransactionFees(transaction: Transaction): BigDecimal {
        val gasPrice = gasPriceService.getGasPrice(transaction.blockNumber, transaction.effectiveGasPrice)
        return gasPrice * transaction.gasUsed.toBigDecimal()
    }

    fun getTransactionBurntFees(transaction: Transaction): BigDecimal? {
        if(chainProperties.isDynamicFeeTarget(transaction.blockNumber)) {
            val transactionFees = getTransactionFees(transaction)
            return transactionFees.divide(BigDecimal(2))
        }
        return null
    }

    fun search(
        transactionSearchRequest: TransactionSearchRequest,
        transactionSearchPageRequest: TransactionSearchPageRequest
    ): Page<Transaction> {
        val simplePageRequest = transactionSearchPageRequest.simplePageRequest()
        val searchedIdPage = transactionSearchClient.searchIds(transactionSearchRequest, transactionSearchPageRequest)
        val transactions = transactionCachedService.getTransactions(searchedIdPage.content)
        return PageImpl(transactions, simplePageRequest.pageRequest(), searchedIdPage.totalElements)
    }

    fun searchByAccountAddress(
        accountAddress: String,
        transactionSearchRequestByAccountAddress: TransactionSearchRequestByAccountAddress,
        transactionSearchPageRequest: TransactionSearchPageRequest
    ): Page<Transaction> {
        val simplePageRequest = transactionSearchPageRequest.simplePageRequest()
        val searchedIdPage = transactionSearchClient.searchIds(
            accountAddress, transactionSearchRequestByAccountAddress, transactionSearchPageRequest)
        val transactions = transactionCachedService.getTransactions(searchedIdPage.content)
        return PageImpl(transactions, simplePageRequest.pageRequest(), searchedIdPage.totalElements)
    }

    fun getAccountKey(transaction: Transaction): AccountKey? =
        if(transaction.type == TransactionType.TxTypeAccountUpdate ||
            transaction.type == TransactionType.TxTypeFeeDelegatedAccountUpdate ||
            transaction.type == TransactionType.TxTypeFeeDelegatedAccountUpdateWithRatio) {
            accountKeyService.getAccountKeyByTransactionHash(transaction.transactionHash)
        } else {
            null
        }
}

@Service
class TransactionCachedService(
    private val accountAddressService: AccountAddressService,
    private val transactionRepository: TransactionRepository,
    private val cacheUtils: CacheUtils,
    private val finderServerPaging: FinderServerPaging
) {
    private val logger = logger(this::class.java)

    private val transactionSort = Sort.by(
        Sort.Order.desc("blockNumber"), Sort.Order.desc("transactionIndex"))

    @Cacheable(cacheNames = [CacheName.TRANSACTION_LATEST_ID])
    fun getMaxTransactionId() = transactionRepository.getMaxTransactionId()

    fun getTransaction(transactionHash: String): Transaction? {
        val transactions = getTransactions(listOf(transactionHash))
        return if (transactions.size == 1) {
            transactions[0]
        } else {
            null
        }
    }

    fun getTransactions(searchTransactionHashes: List<String>) = getTransactions(searchTransactionHashes, true)

    fun getTransactions(searchTransactionHashes: List<String>, fillAccountAddress: Boolean): List<Transaction> {
        val transactionMap =
            cacheUtils.getEntities(
                CacheName.TRANSACTION_BY_HASH,
                Transaction::class.java,
                Transaction::transactionHash,
                searchTransactionHashes,
                transactionRepository::findAllByTransactionHashIn)
        val transactions =
            searchTransactionHashes.filter { transactionMap.containsKey(it) }.mapNotNull { transactionMap[it] }.toList()

        if(fillAccountAddress) {
            accountAddressService.fillAccountAddress(
                transactions.map { it.from }.toList(),
                transactions.mapNotNull { it.to }.toList(),
            )
        }
        return transactions
    }

    fun getTransactionHashesByBlockNumberAndTransactionIndices(blockNumber: Long, transactionIndices: List<Int>) =
        transactionRepository.findByBlockNumberAndTransactionIndexIn(blockNumber, transactionIndices)

    fun getTransactionByBlockNumbersAndTransactionIndices(blockIndexPairs: List<Pair<Long, Int>>) =
        transactionRepository.findAll(Specification<Transaction> { root: Root<Transaction>, query: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder ->
            val predicates = blockIndexPairs.map { (blockNumber, transactionIndex) ->
                criteriaBuilder.and(
                    criteriaBuilder.equal(root.get<Long>("blockNumber"), blockNumber),
                    criteriaBuilder.equal(root.get<Int>("transactionIndex"), transactionIndex)
                )
            }
            criteriaBuilder.or(*predicates.toTypedArray())
        })

    fun getTransactionHashesByAccountAddressAndTransactionType(
        accountAddress: String,
        blockNumberRange: LongRange? = null,
        transactionType: TransactionType?,
        simplePageRequest: SimplePageRequest
    ): List<String> {
        val limit = (simplePageRequest.page * simplePageRequest.size).toLong()
        return if (transactionType != null) {
            if(blockNumberRange != null) {
                transactionRepository.findAllByAccountAddressAndBlockNumberBetweenAndType(
                    accountAddress,
                    blockNumberRange.first, blockNumberRange.last,
                    transactionType,
                    limit, simplePageRequest.pageRequest()).map { it.transactionHash }
                    .toList()
            } else {
                transactionRepository.findAllByAccountAddressAndType(
                    accountAddress, transactionType, limit, simplePageRequest.pageRequest()).map { it.transactionHash }
                    .toList()
            }

        } else {
            if(blockNumberRange != null) {
                transactionRepository.findAllByAccountAddressAndBlockNumberBetween(
                    accountAddress, blockNumberRange.first, blockNumberRange.last,
                    limit, simplePageRequest.pageRequest()).map { it.transactionHash }.toList()
            } else {
                transactionRepository.findAllByAccountAddress(
                    accountAddress, limit, simplePageRequest.pageRequest()).map { it.transactionHash }.toList()
            }
        }
    }

    fun getTransactionHashesByFeePayerAndTransactionType(
        feePayer: String,
        blockNumberRange: LongRange?,
        transactionType: TransactionType?,
        simplePageRequest: SimplePageRequest
    ): List<String> {
        return if (transactionType != null) {
            if(blockNumberRange != null) {
                transactionRepository.findAllByFeePayerAndBlockNumberBetweenAndType(
                    feePayer,
                    blockNumberRange.first, blockNumberRange.last,
                    transactionType,
                    simplePageRequest.pageRequest()
                ).map { it.transactionHash }.toList()
            } else {
                transactionRepository.findAllByFeePayerAndType(
                    feePayer, transactionType, simplePageRequest.pageRequest()
                ).map { it.transactionHash }.toList()
            }

        } else {
            if(blockNumberRange != null) {
                transactionRepository.findAllByFeePayerAndBlockNumberBetween(
                    feePayer,
                    blockNumberRange.first, blockNumberRange.last,
                    simplePageRequest.pageRequest()
                ).map { it.transactionHash }.toList()
            } else {
                transactionRepository.findAllByFeePayer(
                    feePayer, simplePageRequest.pageRequest()
                ).map { it.transactionHash }.toList()
            }
        }
    }

    fun getTransactionHashesByBlockNumberAndTransactionType(
        blockNumber: Long,
        transactionType: TransactionType?,
        simplePageRequest: SimplePageRequest,
    ): Page<TransactionHash> {
        val pageable = simplePageRequest.pageRequest(transactionSort)
        return if (transactionType != null) {
            transactionRepository.findAllByBlockNumberAndType(blockNumber, transactionType, pageable)
        } else {
            transactionRepository.findAllByBlockNumber(blockNumber, pageable)
        }
    }

    fun getTransactionHashesByTransactionType(
        transactionType: TransactionType?,
        simplePageRequest: SimplePageRequest,
    ): List<String> {
        val pageable = simplePageRequest.pageRequest(transactionSort)
        return if (transactionType != null) {
            transactionRepository.findAllByType(transactionType, pageable).map { it.transactionHash }
        } else {
            transactionRepository.findAllBy(pageable).map { it.transactionHash }
        }
    }

    fun getTransactionHashesByBlockRangeAndTransactionType(
        blockRange: LongRange, transactionType: TransactionType?, simplePageRequest: SimplePageRequest
    ): List<String> {
        val pageable = simplePageRequest.pageRequest(transactionSort)
        return if (transactionType != null) {
            transactionRepository.findAllByBlockNumberBetweenAndType(
                blockRange.first, blockRange.last, transactionType, pageable).map { it.transactionHash }
        } else {
            transactionRepository.findAllByBlockNumberBetween(
                blockRange.first, blockRange.last, pageable).map { it.transactionHash }
        }
    }

    fun countByTransactionType(transactionType: TransactionType?) =
        if (transactionType != null) {
            transactionRepository.countAllByType(transactionType, finderServerPaging.limit.transaction)
        } else {
            transactionRepository.countAll(finderServerPaging.limit.transaction)
        }

    fun countByBlockRangeAndTransactionType(blockRange: LongRange, transactionType: TransactionType?) =
        if (transactionType != null) {
            transactionRepository.countAllByBlockNumberBetweenAndType(blockRange.first, blockRange.last, transactionType)
        } else {
            transactionRepository.countAllByBlockNumberBetween(blockRange.first, blockRange.last)
        }

    fun countByAccountAddressAndTransactionType(
        accountAddress: String,
        blockNumberRange: LongRange?,
        transactionType: TransactionType?
    ) =
        minOf(
            finderServerPaging.limit.transaction,
            if (transactionType != null) {
                if(blockNumberRange != null) {
                    transactionRepository.countAllByAccountAddressAndBlockNumberBetweenAndType(
                        accountAddress,
                        blockNumberRange.first, blockNumberRange.last,
                        transactionType,
                        finderServerPaging.limit.transaction)
                } else {
                    transactionRepository.countAllByAccountAddressAndType(accountAddress,
                        transactionType,
                        finderServerPaging.limit.transaction)
                }
            } else {
                if(blockNumberRange != null) {
                    transactionRepository.countAllByAccountAddressAndBlockNumberBetween(
                        accountAddress,
                        blockNumberRange.first, blockNumberRange.last,
                        finderServerPaging.limit.transaction
                    )
                } else {
                    transactionRepository.countAllByAccountAddress(accountAddress, finderServerPaging.limit.transaction)
                }

            })

    fun countByFeePayerAndTransactionType(
        feePayer: String,
        blockNumberRange: LongRange?,
        transactionType: TransactionType?
    ) =
        minOf(
            finderServerPaging.limit.transaction,
            if (transactionType != null) {
                if(blockNumberRange != null) {
                    transactionRepository.countAllByFeePayerAndBlockNumberBetweenAndType(
                        feePayer,
                        blockNumberRange.first, blockNumberRange.last,
                        transactionType,
                        finderServerPaging.limit.transaction)
                } else {
                    transactionRepository.countAllByFeePayerAndType(feePayer,
                        transactionType,
                        finderServerPaging.limit.transaction)
                }
            } else {
                if(blockNumberRange != null) {
                    transactionRepository.countAllByFeePayerAndBlockNumberBetween(
                        feePayer,
                        blockNumberRange.first, blockNumberRange.last,
                        finderServerPaging.limit.transaction
                    )
                } else {
                    transactionRepository.countAllByFeePayer(feePayer, finderServerPaging.limit.transaction)
                }
            })
}