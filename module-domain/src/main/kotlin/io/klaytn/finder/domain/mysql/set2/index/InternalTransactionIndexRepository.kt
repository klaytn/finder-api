package io.klaytn.finder.domain.mysql.set2.index

import io.klaytn.finder.domain.mysql.BaseRepository
import io.klaytn.finder.domain.mysql.InternalTxId
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface InternalTransactionIndexRepository : BaseRepository<InternalTransactionIndex> {
    @Query(
        nativeQuery = true,
        value = """
            SELECT 
                internal_tx_id as internalTxId 
            FROM 
                internal_transaction_index force index(ix_accountaddress_bn_txidx_callid_itxid) 
            WHERE 
                `account_address` = :accountAddress
            ORDER BY 
                block_number DESC, transaction_index DESC, call_id 
            """
    )
    fun findAllByAccountAddress(
        @Param("accountAddress") accountAddress: String,
        pageable: Pageable,
    ): List<InternalTxId>


    @Query(
        nativeQuery = true,
        value = """
            SELECT 
                internal_tx_id as internalTxId 
            FROM 
                internal_transaction_index force index(ix_accountaddress_bn_txidx_callid_itxid) 
            WHERE 
                `account_address` = :accountAddress
            ORDER BY 
                block_number ASC, transaction_index ASC, call_id
            """
    )
    fun findAllByAccountAddressAsc(
        @Param("accountAddress") accountAddress: String,
        pageable: Pageable,
    ): List<InternalTxId>


    @Query(
        nativeQuery = true,
        value = """
            SELECT 
                internal_tx_id as internalTxId 
            FROM 
                internal_transaction_index force index(ix_accountaddress_bn_txidx_callid_itxid) 
            WHERE 
                `account_address` = :accountAddress AND
                block_number between :blockNumberStart and :blockNumberEnd
            ORDER BY 
                block_number DESC, transaction_index DESC, call_id 
            """
    )
    fun findAllByAccountAddressAndBlockNumberBetween(
        @Param("accountAddress") accountAddress: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        pageable: Pageable,
    ): List<InternalTxId>


    @Query(
        nativeQuery = true,
        value = """
            SELECT 
                internal_tx_id as internalTxId 
            FROM 
                internal_transaction_index force index(ix_accountaddress_bn_txidx_callid_itxid) 
            WHERE 
                `account_address` = :accountAddress AND
                block_number between :blockNumberStart and :blockNumberEnd
            ORDER BY 
                block_number ASC, transaction_index ASC, call_id 
            """
    )
    fun findAllByAccountAddressAndBlockNumberBetweenAsc(
        @Param("accountAddress") accountAddress: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        pageable: Pageable,
    ): List<InternalTxId>

    @Query(
        nativeQuery = true,
        value = """
            SELECT 
                count(*) 
            FROM (
                SELECT 
                    1
                FROM 
                    internal_transaction_index 
                WHERE 
                    `account_address` = :accountAddress
                LIMIT 
                    :maxTotalCount            
            ) t1
            """
    )
    fun countAllByAccountAddress(
        @Param("accountAddress") accountAddress: String,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long

    @Query(
        nativeQuery = true,
        value = """
            SELECT 
                count(*) 
            FROM (
                SELECT 
                    1
                FROM 
                    internal_transaction_index 
                WHERE 
                    `account_address` = :accountAddress AND
                    block_number between :blockNumberStart and :blockNumberEnd
                LIMIT 
                    :maxTotalCount            
            ) t1
            """
    )
    fun countAllByAccountAddressAndBlockNumberBetween(
        @Param("accountAddress") accountAddress: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long

    fun existsByAccountAddress(accountAddress: String): Boolean
}