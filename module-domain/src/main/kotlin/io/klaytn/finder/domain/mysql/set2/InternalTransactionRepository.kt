package io.klaytn.finder.domain.mysql.set2

import io.klaytn.finder.domain.mysql.BaseRepository
import io.klaytn.finder.domain.mysql.InternalTxId
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface InternalTransactionRepository : BaseRepository<InternalTransaction> {
    fun findAllByInternalTxIdIn(ids: List<String>): List<InternalTransaction>
    fun findAllByBlockNumberOrderByTransactionIndexDescCallIdAsc(
        blockNumber: Long,
        pageable: Pageable,
    ): List<InternalTxId>

    fun findAllByBlockNumberAndTransactionIndexOrderByCallIdAsc(
        blockNumber: Long,
        transactionIndex: Int,
        pageable: Pageable,
    ): List<InternalTxId>

    @Query(
        nativeQuery = true,
        value = """
            SELECT count(*) FROM (
                SELECT 1 FROM internal_transactions WHERE block_number = :blockNumber limit :maxTotalCount
            ) t
        """
    )
    fun countAllByBlockNumber(
        @Param("blockNumber") blockNumber: Long,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long

    @Query(
        nativeQuery = true,
        value = """
            SELECT count(*) FROM (
                SELECT 1 FROM internal_transactions WHERE block_number = :blockNumber AND transaction_index = :transactionIndex limit :maxTotalCount
            ) t
        """
    )
    fun countAllByBlockNumberAndTransactionIndex(
        @Param("blockNumber") blockNumber: Long,
        @Param("transactionIndex") transactionIndex: Int,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long
}
