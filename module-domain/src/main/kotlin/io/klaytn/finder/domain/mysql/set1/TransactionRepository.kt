package io.klaytn.finder.domain.mysql.set1

import com.klaytn.caver.transaction.type.TransactionType
import io.klaytn.finder.domain.mysql.BaseRepository
import io.klaytn.finder.infra.db.DbTableConstants
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

const val etherscanLikeAccountTransactionSelectQuery: String = """
    SELECT DISTINCT
        (t.transaction_hash) AS `hash`,
        CAST(t.block_number AS CHAR) AS blockNumber,
        CAST(t.timestamp AS CHAR) AS timeStamp,
        t.nonce,
        t.block_hash AS blockHash,
        CAST(t.transaction_index AS CHAR) AS transactionIndex,
        t.from,
        t.to,
        CAST(t.value AS CHAR) AS value,
        CAST(t.gas AS CHAR) AS gas,
        CAST(t.effective_gas_price AS CHAR) AS gasPrice,
        IF(t.tx_error IS NULL, '0', '1') AS isError,
        CAST(t.status AS CHAR) AS txreceipt_status,
        IFNULL(t.input, ""),
        IFNULL(t.contract_address, "") AS contractAddress,
        CAST(t.gas_used AS CHAR) AS gasUsed,
        CAST(((SELECT b.number FROM blocks b ORDER BY b.number DESC LIMIT 1) - t.block_number) AS CHAR) AS confirmations,
        IFNULL(left(t.input, 10), "") AS methodId,
        IFNULL(fs.text_signature, "") AS functionName
        FROM
        ${DbTableConstants.transactions} t
        LEFT JOIN function_signatures fs ON fs.bytes_signature = left(t.input, 10) AND fs. `primary` = TRUE
    """
const val etherscanLikeCumulativeGasUsedQuery: String = """
    CAST((
	SELECT
		SUM(t2.gas_used) AS gas FROM ${DbTableConstants.transactions} t2
WHERE
	t2.block_number = T1.blockNumber 
    AND t2.transaction_index <= T1.transactionIndex
    ) AS CHAR) AS cumulativeGasUsed
"""
const val etherscanLikeAccountTransactionWhereFromQuery: String = "WHERE t.from = :accountAddress"
const val etherscanLikeAccountTransactionWhereToQuery: String = "WHERE t.to = :accountAddress"
const val etherscanLikeAccountTransactionBlockNumberBetweenQuery: String = " AND t.block_number BETWEEN :blockNumberStart AND :blockNumberEnd "
const val etherscanLikeAccountTransactionOrderByDescQuery: String = "ORDER BY t.block_number DESC, t.transaction_index DESC"
const val etherscanLikeAccountTransactionOrderByAscQuery: String = "ORDER BY t.block_number ASC, t.transaction_index ASC"
const val etherscanLikeAccountTransactionLimitQuery: String = "LIMIT :maxTotalCount"

const val etherscanLikeAccountTransactionQueryOrderDescQuery: String = """
    SELECT T1.*, $etherscanLikeCumulativeGasUsedQuery FROM (
    (
    $etherscanLikeAccountTransactionSelectQuery
    $etherscanLikeAccountTransactionWhereFromQuery
    $etherscanLikeAccountTransactionOrderByDescQuery
    $etherscanLikeAccountTransactionLimitQuery
    )
    UNION
    (
    $etherscanLikeAccountTransactionSelectQuery
    $etherscanLikeAccountTransactionWhereToQuery
    $etherscanLikeAccountTransactionOrderByDescQuery
    $etherscanLikeAccountTransactionLimitQuery
    )
    ) T1 
    ORDER BY T1.blockNumber DESC, T1.transactionIndex DESC
    LIMIT :offset, :limit
    """
const val etherscanLikeAccountTransactionQueryOrderAscQuery: String = """
    SELECT T1.*, $etherscanLikeCumulativeGasUsedQuery FROM (
    (
    $etherscanLikeAccountTransactionSelectQuery
    $etherscanLikeAccountTransactionWhereFromQuery
    $etherscanLikeAccountTransactionOrderByAscQuery
    $etherscanLikeAccountTransactionLimitQuery
    )
    UNION
    (
    $etherscanLikeAccountTransactionSelectQuery
    $etherscanLikeAccountTransactionWhereToQuery
    $etherscanLikeAccountTransactionOrderByAscQuery
    $etherscanLikeAccountTransactionLimitQuery
    )
    ) T1 
    ORDER BY T1.blockNumber ASC, T1.transactionIndex ASC
    LIMIT :offset, :limit 
    """
const val etherscanLikeAccountTransactionBlockNumberBetweenOrderByDescQuery = """
    SELECT T1.*, $etherscanLikeCumulativeGasUsedQuery FROM (
    (
    $etherscanLikeAccountTransactionSelectQuery
    $etherscanLikeAccountTransactionWhereFromQuery
    $etherscanLikeAccountTransactionBlockNumberBetweenQuery
    $etherscanLikeAccountTransactionOrderByDescQuery
    $etherscanLikeAccountTransactionLimitQuery
    )
    UNION
    (
    $etherscanLikeAccountTransactionSelectQuery
    $etherscanLikeAccountTransactionWhereToQuery
    $etherscanLikeAccountTransactionBlockNumberBetweenQuery
    $etherscanLikeAccountTransactionOrderByDescQuery
    $etherscanLikeAccountTransactionLimitQuery
    )
    ) T1 
    ORDER BY T1.blockNumber DESC, T1.transactionIndex DESC
    LIMIT :offset, :limit
    """
const val etherscanLikeAccountTransactionBlockNumberBetweenOrderByAscQuery = """
    SELECT T1.*, $etherscanLikeCumulativeGasUsedQuery FROM (
    (
    $etherscanLikeAccountTransactionSelectQuery
    $etherscanLikeAccountTransactionWhereFromQuery
    $etherscanLikeAccountTransactionBlockNumberBetweenQuery
    $etherscanLikeAccountTransactionOrderByAscQuery
    $etherscanLikeAccountTransactionLimitQuery
    )
    UNION
    (
    $etherscanLikeAccountTransactionSelectQuery
    $etherscanLikeAccountTransactionWhereToQuery
    $etherscanLikeAccountTransactionBlockNumberBetweenQuery
    $etherscanLikeAccountTransactionOrderByAscQuery
    $etherscanLikeAccountTransactionLimitQuery
    )
    ) T1 
    ORDER BY T1.blockNumber ASC, T1.transactionIndex ASC
    LIMIT :offset, :limit
    """

const val etherscanLikeAccountTransactionWhereInTxHashQuery: String = "WHERE t.transaction_hash IN :transactionHashes"
const val etherscanLikeAccountTokenTransactionQuery: String = """ 
    SELECT T1.*, $etherscanLikeCumulativeGasUsedQuery FROM 
    (
    $etherscanLikeAccountTransactionSelectQuery
    $etherscanLikeAccountTransactionWhereInTxHashQuery
    ) T1
"""
@Repository
interface TransactionRepository : BaseRepository<Transaction> {
    @Query(
        nativeQuery = true,
        value = "SELECT max(id) FROM ${DbTableConstants.transactions}"
    )
    fun getMaxTransactionId(): Long

    @Modifying
    @Transactional
    @Query(
        nativeQuery = true,
        value = "SET session sort_buffer_size = :bufferSize"
    )
    fun setSessionBufferSize(bufferSize: Long = 4024024L): Any


    fun findAllByBlockNumber(blockNumber: Long, pageable: Pageable): Page<TransactionHash>
    fun findAllByBlockNumberAndType(
        blockNumber: Long,
        transactionType: TransactionType,
        pageable: Pageable,
    ): Page<TransactionHash>

    fun findAllByTransactionHashIn(transactionHashes: List<String>): List<Transaction>

    fun findByBlockNumberAndTransactionIndexIn(
        blockNumber: Long,
        transactionIndices: List<Int>,
    ): List<TransactionHash>

    fun findAllBy(pageable: Pageable): List<TransactionHash>
    fun findAllByType(transactionType: TransactionType, pageable: Pageable): List<TransactionHash>

    fun findAllByBlockNumberBetween(blockNumberStart: Long, blockNumberEnd: Long, pageable: Pageable): List<TransactionHash>
    fun findAllByBlockNumberBetweenAndType(blockNumberStart: Long, blockNumberEnd: Long, transactionType: TransactionType, pageable: Pageable): List<TransactionHash>

    @Query(
        nativeQuery = true,
        value = """
            (
                (
                    SELECT 
                        transaction_hash as transactionHash, block_number, transaction_index 
                    FROM 
                        ${DbTableConstants.transactions} force index(ix_from_type_blocknumber_transactionindex)
                    WHERE 
                        `from` = :accountAddress AND `type` = :#{#type.name()} 
                    ORDER BY block_number DESC, transaction_index DESC limit :maxTotalCount)
                union 
                (
                    SELECT 
                        transaction_hash as transactionHash, block_number, transaction_index 
                    FROM 
                        ${DbTableConstants.transactions} force index(ix_to_type_blocknumber_transactionindex)
                    WHERE 
                        `to` = :accountAddress AND `type` = :#{#type.name()} 
                    ORDER BY block_number DESC, transaction_index DESC limit :maxTotalCount)
            )
            ORDER BY block_number DESC, transaction_index DESC
            """
    )
    fun findAllByAccountAddressAndType(
        @Param("accountAddress") accountAddress: String,
        @Param("type") transactionType: TransactionType,
        @Param("maxTotalCount") maxTotalCount: Long,
        pageable: Pageable,
    ): List<TransactionHash>

    @Query(
        nativeQuery = true,
        value = """
            (
                (
                    SELECT 
                        transaction_hash as transactionHash, block_number, transaction_index 
                    FROM 
                        ${DbTableConstants.transactions} force index(ix_from_type_blocknumber_transactionindex)
                    WHERE 
                        `from` = :accountAddress  
                        AND block_number between :blockNumberStart and :blockNumberEnd
                        AND `type` = :#{#type.name()}
                    ORDER BY block_number DESC, transaction_index DESC limit :maxTotalCount)
                union 
                (
                    SELECT 
                        transaction_hash as transactionHash, block_number, transaction_index 
                    FROM 
                        ${DbTableConstants.transactions} force index(ix_to_type_blocknumber_transactionindex)
                    WHERE 
                        `to` = :accountAddress
                        AND block_number between :blockNumberStart and :blockNumberEnd
                        AND `type` = :#{#type.name()} 
                    ORDER BY block_number DESC, transaction_index DESC limit :maxTotalCount)
            )
            ORDER BY block_number DESC, transaction_index DESC
            """
    )
    fun findAllByAccountAddressAndBlockNumberBetweenAndType(
        @Param("accountAddress") accountAddress: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        @Param("type") transactionType: TransactionType,
        @Param("maxTotalCount") maxTotalCount: Long,
        pageable: Pageable,
    ): List<TransactionHash>

    @Query(
        nativeQuery = true,
        value = """
            (
                (
                    SELECT 
                        transaction_hash as transactionHash, block_number, transaction_index 
                    FROM 
                        ${DbTableConstants.transactions} force index(ix_from_blocknumber_transactionindex) 
                    WHERE 
                        `from` = :accountAddress 
                    ORDER BY block_number DESC, transaction_index DESC limit :maxTotalCount)
                union 
                (
                    SELECT 
                        transaction_hash as transactionHash, block_number, transaction_index 
                    FROM 
                        ${DbTableConstants.transactions} force index(ix_to_blocknumber_transactionindex)
                    WHERE 
                        `to` = :accountAddress 
                    ORDER BY block_number DESC, transaction_index DESC limit :maxTotalCount)
            )
            ORDER BY block_number DESC, transaction_index DESC
            """
    )
    fun findAllByAccountAddress(
        @Param("accountAddress") accountAddress: String,
        @Param("maxTotalCount") maxTotalCount: Long,
        pageable: Pageable,
    ): List<TransactionHash>

    @Query(
        nativeQuery = true,
        value = """
            (
                (
                    SELECT 
                        transaction_hash as transactionHash, block_number, transaction_index 
                    FROM 
                        ${DbTableConstants.transactions} force index(ix_from_blocknumber_transactionindex) 
                    WHERE 
                        `from` = :accountAddress 
                        AND block_number between :blockNumberStart and :blockNumberEnd
                    ORDER BY block_number DESC, transaction_index DESC limit :maxTotalCount)
                union 
                (
                    SELECT 
                        transaction_hash as transactionHash, block_number, transaction_index 
                    FROM 
                        ${DbTableConstants.transactions} force index(ix_to_blocknumber_transactionindex)
                    WHERE 
                        `to` = :accountAddress 
                        AND block_number between :blockNumberStart and :blockNumberEnd
                    ORDER BY block_number DESC, transaction_index DESC limit :maxTotalCount)
            )
            ORDER BY block_number DESC, transaction_index DESC
            """
    )
    fun findAllByAccountAddressAndBlockNumberBetween(
        @Param("accountAddress") accountAddress: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        @Param("maxTotalCount") maxTotalCount: Long,
        pageable: Pageable,
    ): List<TransactionHash>

    @Query(
        nativeQuery = true,
        value = """
            SELECT 
                transaction_hash as transactionHash, block_number, transaction_index 
            FROM 
                ${DbTableConstants.transactions} 
            WHERE 
                `fee_payer` = :feePayer 
                AND `type` = :#{#type.name()} 
            ORDER BY 
                block_number DESC, transaction_index DESC
            """
    )
    fun findAllByFeePayerAndType(
        @Param("feePayer") feePayer: String,
        @Param("type") transactionType: TransactionType,
        pageable: Pageable,
    ): List<TransactionHash>

    @Query(
        nativeQuery = true,
        value = """
            SELECT 
                transaction_hash as transactionHash, block_number, transaction_index 
            FROM 
                ${DbTableConstants.transactions} 
            WHERE 
                `fee_payer` = :feePayer 
                AND `type` = :#{#type.name()} 
                AND block_number between :blockNumberStart and :blockNumberEnd
            ORDER BY 
                block_number DESC, transaction_index DESC
            """
    )
    fun findAllByFeePayerAndBlockNumberBetweenAndType(
        @Param("feePayer") feePayer: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        @Param("type") transactionType: TransactionType,
        pageable: Pageable,
    ): List<TransactionHash>

    @Query(
        nativeQuery = true,
        value = """
            SELECT 
                transaction_hash as transactionHash, block_number, transaction_index 
            FROM 
                ${DbTableConstants.transactions} 
            WHERE 
                `fee_payer` = :feePayer 
            ORDER BY 
                block_number DESC, transaction_index DESC
            """
    )
    fun findAllByFeePayer(
        @Param("feePayer") feePayer: String,
        pageable: Pageable,
    ): List<TransactionHash>

    @Query(
        nativeQuery = true,
        value = """
            SELECT 
                transaction_hash as transactionHash, block_number, transaction_index 
            FROM 
                ${DbTableConstants.transactions} 
            WHERE 
                `fee_payer` = :feePayer 
                AND block_number between :blockNumberStart and :blockNumberEnd
            ORDER BY 
                block_number DESC, transaction_index DESC
            """
    )
    fun findAllByFeePayerAndBlockNumberBetween(
        @Param("feePayer") feePayer: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        pageable: Pageable,
    ): List<TransactionHash>

    @Query(
        nativeQuery = true,
        value = etherscanLikeAccountTransactionQueryOrderDescQuery
    )
    fun findAllByAccountAddressEtherscanLikeDesc(
        @Param("accountAddress") accountAddress: String,
        @Param("maxTotalCount") maxTotalCount: Long,
        @Param("offset") offset: Long,
        @Param("limit") limit: Long,
    ): List<List<String>>

    @Query(
        nativeQuery = true,
        value = etherscanLikeAccountTransactionQueryOrderAscQuery
    )
    fun findAllByAccountAddressEtherscanLikeAsc(
        @Param("accountAddress") accountAddress: String,
        @Param("maxTotalCount") maxTotalCount: Long,
        @Param("offset") offset: Long,
        @Param("limit") limit: Long,
    ):List<List<String>>

    @Query(
        nativeQuery = true,
        value = etherscanLikeAccountTransactionBlockNumberBetweenOrderByDescQuery
    )
    fun findAllByAccountAddressBlockNumberBetweenEtherscanLikeDesc(
        @Param("accountAddress") accountAddress: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        @Param("maxTotalCount") maxTotalCount: Long,
        @Param("offset") offset: Long,
        @Param("limit") limit: Long,
    ):List<List<String>>

    @Query(
        nativeQuery = true,
        value = etherscanLikeAccountTransactionBlockNumberBetweenOrderByAscQuery
    )
    fun findAllByAccountAddressBlockNumberBetweenEtherscanLikeAsc(
        @Param("accountAddress") accountAddress: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        @Param("maxTotalCount") maxTotalCount: Long,
        @Param("offset") offset: Long,
        @Param("limit") limit: Long,
    ):List<List<String>>

    @Query(
        nativeQuery = true,
        value = etherscanLikeAccountTokenTransactionQuery
    )
    fun findAllByTransactionHashInEtherscanLike(
        @Param("transactionHashes") transactionHashes: List<String>,
    ): List<List<String>>

    @Query(value = """
        SELECT * FROM (SELECT CONCAT(t.block_number, ',', t.transaction_index) AS blocksTxIndices, t.* FROM ${DbTableConstants.transactions} t WHERE t.block_number IN :blocks) T1
        WHERE T1.blocksTxIndices IN :blocksTxIndices""", nativeQuery = true)
    fun findAllByTransactionByBlockNumbersAndTransactionIndices(
        @Param("blocks") blocks:  List<Long>,
        @Param("blocksTxIndices") blocksTxIndices:  List<String>
    ): List<Transaction>



    // -- --------------------------------------------------------------------------------------------------------------
    // -- counts
    // -- --------------------------------------------------------------------------------------------------------------

    @Query(
        nativeQuery = true,
        value = """
            SELECT count(*) FROM (
                SELECT 1 FROM ${DbTableConstants.transactions} limit :maxTotalCount
            ) t
        """
    )
    fun countAll(@Param("maxTotalCount") maxTotalCount: Long): Long

    @Query(
        nativeQuery = true,
        value = """
            SELECT count(*) FROM (
                SELECT 1 FROM ${DbTableConstants.transactions} WHERE `type`= :#{#type.name()} limit :maxTotalCount
            ) t
        """
    )
    fun countAllByType(
        @Param("type") transactionType: TransactionType,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long


    @Query(
        nativeQuery = true,
        value = """
            SELECT count(*) FROM (
                SELECT 
                    1 
                FROM 
                    ${DbTableConstants.transactions} 
                WHERE 
                    block_number between :blockNumberStart AND :blockNumberEnd 
                    AND `type`= :#{#type.name()} 
            ) t
        """
    )
    fun countAllByBlockNumberBetweenAndType(
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        @Param("type") transactionType: TransactionType,
    ): Long

    @Query(
        nativeQuery = true,
        value = """
            SELECT count(*) FROM (
                SELECT 
                    1 
                FROM 
                    ${DbTableConstants.transactions} 
                WHERE 
                    block_number between :blockNumberStart AND :blockNumberEnd  
            ) t
        """
    )
    fun countAllByBlockNumberBetween(
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
    ): Long

    @Query(
        nativeQuery = true,
        value = """
            SELECT count(DISTINCT id) as cnt FROM (
                (
                    SELECT 
                        id
                    FROM 
                        ${DbTableConstants.transactions} force index(ix_from_blocknumber_transactionindex)
                    WHERE 
                        `from` = :accountAddress
                    limit :maxTotalCount
                ) 
                union all
                (
                    SELECT 
                        id
                    FROM 
                        ${DbTableConstants.transactions} force index(ix_to_blocknumber_transactionindex)
                    WHERE 
                        `to` = :accountAddress  
                    limit :maxTotalCount
                )
            ) as s         
            """,
    )
    fun countAllByAccountAddress(
        @Param("accountAddress") accountAddress: String,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long

    @Query(
        nativeQuery = true,
        value = """
            SELECT count(DISTINCT id) as cnt FROM (
                (
                    SELECT 
                        id
                    FROM 
                        ${DbTableConstants.transactions} force index(ix_from_blocknumber_transactionindex)
                    WHERE 
                        `from` = :accountAddress
                        AND block_number between :blockNumberStart and :blockNumberEnd
                    limit :maxTotalCount
                ) 
                union all
                (
                    SELECT 
                        id
                    FROM 
                        ${DbTableConstants.transactions} force index(ix_to_blocknumber_transactionindex)
                    WHERE 
                        `to` = :accountAddress  
                        AND block_number between :blockNumberStart and :blockNumberEnd
                    limit :maxTotalCount
                )
            ) as s         
            """,
    )
    fun countAllByAccountAddressAndBlockNumberBetween(
        @Param("accountAddress") accountAddress: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long

    @Query(
        nativeQuery = true,
        value = """
            SELECT count(DISTINCT id) as cnt FROM (
                (
                    SELECT 
                        id
                    FROM 
                        ${DbTableConstants.transactions} force index(ix_from_type_blocknumber_transactionindex)
                    WHERE 
                        `from` = :accountAddress  AND `type`= :#{#type.name()}
                    limit :maxTotalCount
                )
                union all
                (
                    SELECT 
                        id
                    FROM 
                        ${DbTableConstants.transactions} force index(ix_to_type_blocknumber_transactionindex)
                    WHERE 
                        `to` = :accountAddress  AND `type`= :#{#type.name()}
                    limit :maxTotalCount
                )
            ) as s      
            """,
    )
    fun countAllByAccountAddressAndType(
        @Param("accountAddress") accountAddress: String,
        @Param("type") transactionType: TransactionType,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long

    @Query(
        nativeQuery = true,
        value = """
            SELECT count(DISTINCT id) as cnt FROM (
                (
                    SELECT 
                        id
                    FROM 
                        ${DbTableConstants.transactions} force index(ix_from_type_blocknumber_transactionindex)
                    WHERE 
                        `from` = :accountAddress  
                        AND block_number between :blockNumberStart and :blockNumberEnd
                        AND `type`= :#{#type.name()}
                    limit :maxTotalCount
                )
                union all
                (
                    SELECT 
                        id
                    FROM 
                        ${DbTableConstants.transactions} force index(ix_to_type_blocknumber_transactionindex)
                    WHERE 
                        `to` = :accountAddress  
                        AND block_number between :blockNumberStart and :blockNumberEnd
                        AND `type`= :#{#type.name()}
                    limit :maxTotalCount
                )
            ) as s      
            """,
    )
    fun countAllByAccountAddressAndBlockNumberBetweenAndType(
        @Param("accountAddress") accountAddress: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        @Param("type") transactionType: TransactionType,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long

    @Query(
        nativeQuery = true,
        value = """
            SELECT count(*) as CNT FROM (
                SELECT 
                    1 
                FROM 
                    ${DbTableConstants.transactions} 
                WHERE 
                    `fee_payer` = :feePayer  
                    AND `type`= :#{#type.name()} 
                limit 
                    :maxTotalCount
            ) t
            """,
    )
    fun countAllByFeePayerAndType(
        @Param("feePayer") feePayer: String,
        @Param("type") transactionType: TransactionType,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long

    @Query(
        nativeQuery = true,
        value = """
            SELECT count(*) as CNT FROM (
                SELECT 
                    1 
                FROM 
                    ${DbTableConstants.transactions} 
                WHERE 
                    `fee_payer` = :feePayer 
                    AND block_number between :blockNumberStart and :blockNumberEnd
                    AND `type`= :#{#type.name()} 
                limit :maxTotalCount
            ) t
            """,
    )
    fun countAllByFeePayerAndBlockNumberBetweenAndType(
        @Param("feePayer") feePayer: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        @Param("type") transactionType: TransactionType,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long

    @Query(
        nativeQuery = true,
        value = """
            SELECT count(*) as CNT FROM (
                SELECT 
                    1 
                FROM 
                    ${DbTableConstants.transactions} 
                WHERE 
                    `fee_payer` = :feePayer 
                limit 
                    :maxTotalCount
            ) t
            """,
    )
    fun countAllByFeePayer(
        @Param("feePayer") feePayer: String,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long

    @Query(
        nativeQuery = true,
        value = """
            SELECT count(*) as CNT FROM (
                SELECT 
                    1 
                FROM 
                    ${DbTableConstants.transactions} 
                WHERE 
                    `fee_payer` = :feePayer 
                    AND block_number between :blockNumberStart and :blockNumberEnd
                limit 
                    :maxTotalCount
            ) t
            """,
    )
    fun countAllByFeePayerAndBlockNumberBetween(
        @Param("feePayer") feePayer: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long

    @Query(
        nativeQuery = true,
        value = """
            SELECT
                CASE WHEN EXISTS (
                (
                    (select id from ${DbTableConstants.transactions} where `from` = :accountAddress limit 1)
                    union
                    (select id from ${DbTableConstants.transactions} where `to` = :accountAddress limit 1)
                )
            )
            THEN 'true'
            ELSE 'false'
            END            
        """
    )
    fun existsByAccountAddress(@Param("accountAddress") accountAddress: String): Boolean
    fun existsByFeePayer(feePayer: String): Boolean

}

interface TransactionHash {
    val transactionHash: String
}