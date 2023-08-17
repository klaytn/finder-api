package io.klaytn.finder.service

import io.klaytn.finder.config.dynamic.FinderServerPaging
import io.klaytn.finder.domain.mysql.InternalTxId
import io.klaytn.finder.domain.mysql.set2.InternalTransaction
import io.klaytn.finder.domain.mysql.set2.InternalTransactionRepository
import io.klaytn.finder.infra.cache.CacheName
import io.klaytn.finder.infra.cache.CacheUtils
import io.klaytn.finder.infra.db.shard.ShardNum
import io.klaytn.finder.infra.db.shard.ShardNumContextHolder
import io.klaytn.finder.infra.db.shard.selector.BlockShardNumSelector
import io.klaytn.finder.infra.utils.PageUtils
import io.klaytn.finder.infra.web.model.SimplePageRequest
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
class InternalTransactionService(
    private val internalTransactionCachedService: InternalTransactionCachedService,
    private val internalTransactionIndexService: InternalTransactionIndexService,
) {
    fun getInternalTransactionsByBlockNumber(
        blockNumber: Long,
        simplePageRequest: SimplePageRequest,
    ): Page<InternalTransaction> {
        val totalCount = internalTransactionCachedService.countByBlockNumber(blockNumber)
        PageUtils.checkPageParameter(simplePageRequest, totalCount)

        val contents = internalTransactionCachedService.getIdsByBlockNumber(blockNumber, simplePageRequest)
            .map { it.internalTxId }.run { internalTransactionCachedService.getInternalTransactions(this) }
        return PageUtils.getPage(contents, simplePageRequest, totalCount)
    }

    fun getInternalTransactionsByBlockNumberAndIndex(
        blockNumber: Long,
        transactionIndex: Int,
        simplePageRequest: SimplePageRequest,
    ): Page<InternalTransaction> {
        val totalCount =
            internalTransactionCachedService.countByBlockNumberAndTransactionIndex(blockNumber, transactionIndex)
        PageUtils.checkPageParameter(simplePageRequest, totalCount)

        val contents = internalTransactionCachedService.getIdsByBlockNumberAndTransactionIndex(blockNumber,
            transactionIndex, simplePageRequest).map { it.internalTxId }
            .run { internalTransactionCachedService.getInternalTransactions(this) }
        return PageUtils.getPage(contents, simplePageRequest, totalCount)
    }

    /**
     * Retrieve a list of internal transactions associated with the account address (EOA, SCA).
     */
    fun getInternalTransactionsByAccountAddress(
        accountAddress: String,
        blockNumberRange: LongRange? = null,
        simplePageRequest: SimplePageRequest,
    ): Page<InternalTransaction> {
        val totalCount =
            internalTransactionIndexService.countByAccountAddress(accountAddress, blockNumberRange)
        PageUtils.checkPageParameter(simplePageRequest, totalCount)

        val contents = internalTransactionIndexService.getIdsByAccountAddress(
            accountAddress, blockNumberRange, simplePageRequest)
            .map { it.internalTxId }
            .run { internalTransactionCachedService.getInternalTransactions(this) }
        return PageUtils.getPage(contents, simplePageRequest, totalCount)
    }

    fun existsByAccountAddress(accountAddress: String) =
        internalTransactionIndexService.existsByAccountAddress(accountAddress)
}

@Service
class InternalTransactionCachedService(
    private val set2BlockShardNumSelector: BlockShardNumSelector,
    private val accountAddressService: AccountAddressService,
    private val internalTransactionRepository: InternalTransactionRepository,
    private val cacheUtils: CacheUtils,
    private val finderServerPaging: FinderServerPaging
) {
    // -----------------------------------------------------------------------------------------------------------------
    // -- internal-tx
    // -----------------------------------------------------------------------------------------------------------------

    fun getInternalTransactions(searchIds: List<String>): List<InternalTransaction> {
        val shardMap = mutableMapOf<ShardNum, MutableSet<String>>()
        searchIds.forEach {
            val blockNumber = it.split("_")[0].toLong()
            val set2DataSourceType = set2BlockShardNumSelector.getShardType(blockNumber)

            val idSet = shardMap.getOrPut(set2DataSourceType) { mutableSetOf() }
            idSet.add(it)
        }

        val internalTransactionsMap = mutableMapOf<String, InternalTransaction>()
        shardMap.forEach {
            ShardNumContextHolder.setDataSourceType(it.key)
            try {
                internalTransactionsMap.putAll(
                    cacheUtils.getEntities(CacheName.INTERNAL_TRANSACTION,
                        InternalTransaction::class.java,
                        InternalTransaction::internalTxId,
                        it.value,
                        internalTransactionRepository::findAllByInternalTxIdIn
                    )
                )
            } finally {
                ShardNumContextHolder.clear()
            }
        }

        accountAddressService.fillAccountAddress(
            internalTransactionsMap.values.map { it.from }.toList(),
            internalTransactionsMap.values.mapNotNull { it.to }.toList(),
        )
        return searchIds.filter { internalTransactionsMap.containsKey(it) }.mapNotNull { internalTransactionsMap[it] }
            .toList()
    }

    fun getIdsByBlockNumber(blockNumber: Long, simplePageRequest: SimplePageRequest): List<InternalTxId> {
        try {
            ShardNumContextHolder.setDataSourceType(set2BlockShardNumSelector.getShardType(blockNumber))
            return internalTransactionRepository.findAllByBlockNumberOrderByTransactionIndexDescCallIdAsc(blockNumber,
                simplePageRequest.pageRequest())
        } finally {
            ShardNumContextHolder.clear()
        }
    }

    fun getIdsByBlockNumberAndTransactionIndex(
        blockNumber: Long, transactionIndex: Int, simplePageRequest: SimplePageRequest,
    ): List<InternalTxId> {
        try {
            ShardNumContextHolder.setDataSourceType(set2BlockShardNumSelector.getShardType(blockNumber))
            return internalTransactionRepository.findAllByBlockNumberAndTransactionIndexOrderByCallIdAsc(
                blockNumber, transactionIndex, simplePageRequest.pageRequest())
        } finally {
            ShardNumContextHolder.clear()
        }
    }

    fun countByBlockNumber(blockNumber: Long): Long {
        try {
            ShardNumContextHolder.setDataSourceType(set2BlockShardNumSelector.getShardType(blockNumber))
            return internalTransactionRepository.countAllByBlockNumber(blockNumber, finderServerPaging.limit.internalTransaction)
        } finally {
            ShardNumContextHolder.clear()
        }
    }

    fun countByBlockNumberAndTransactionIndex(blockNumber: Long, transactionIndex: Int): Long {
        try {
            ShardNumContextHolder.setDataSourceType(set2BlockShardNumSelector.getShardType(blockNumber))
            return internalTransactionRepository.countAllByBlockNumberAndTransactionIndex(blockNumber,
                transactionIndex,
                finderServerPaging.limit.internalTransaction)
        } finally {
            ShardNumContextHolder.clear()
        }
    }
}