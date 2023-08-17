package io.klaytn.finder.domain.mysql.set3

import io.klaytn.finder.domain.mysql.BaseRepository
import io.klaytn.finder.infra.db.DbTableConstants
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import io.klaytn.finder.domain.mysql.set3.token.TokenTransfer

const val etherscanLikeAccountTokenTransactionSelectQuery: String = """
    SELECT
        t.transaction_hash AS `hash`,
        CAST(t.block_number AS CHAR) AS blockNumber,
        CAST(t.timestamp AS CHAR) AS timeStamp,
        t.from,
        t.to,
        t.contract_address AS contractAddress,
        t.amount AS value,
        t.display_order AS displayOrder
        FROM
        ${DbTableConstants.tokenTransfers} t
    """
const val etherscanLikeAccountTokenTransactionWhereFromQuery: String = "WHERE t.from = :accountAddress"
const val etherscanLikeAccountTokenTransactionWhereToQuery: String = "WHERE t.to = :accountAddress"
const val etherscanLikeAccountTokenTransactionWhereAndContractAddressQuery: String = "AND t.contract_address = :contractAddress"
const val etherscanLikeAccountTokenTransactionBlockNumberBetweenQuery: String = " AND t.block_number BETWEEN :blockNumberStart AND :blockNumberEnd "
const val etherscanLikeAccountTokenTransactionOrderByDescQuery: String = "ORDER BY t.block_number DESC, t.display_order DESC"
const val etherscanLikeAccountTokenTransactionOrderByAscQuery: String = "ORDER BY t.block_number ASC, t.display_order ASC"
const val etherscanLikeAccountTokenTransactionLimitQuery: String = "LIMIT :maxTotalCount"

const val etherscanLikeAccountTokenTransactionQueryOrderDescQuery: String = """
    SELECT T1.* FROM (
    (
    $etherscanLikeAccountTokenTransactionSelectQuery
    $etherscanLikeAccountTokenTransactionWhereFromQuery
    $etherscanLikeAccountTokenTransactionOrderByDescQuery
    $etherscanLikeAccountTokenTransactionLimitQuery
    )
    UNION
    (
    $etherscanLikeAccountTokenTransactionSelectQuery
    $etherscanLikeAccountTokenTransactionWhereToQuery
    $etherscanLikeAccountTokenTransactionOrderByDescQuery
    $etherscanLikeAccountTokenTransactionLimitQuery
    )
    ) T1 
    ORDER BY T1.blockNumber DESC, T1.displayOrder DESC
    LIMIT :offset, :limit
    """
const val etherscanLikeAccountTokenTransactionQueryOrderAscQuery: String = """
    SELECT T1.* FROM (
    (
    $etherscanLikeAccountTokenTransactionSelectQuery
    $etherscanLikeAccountTokenTransactionWhereFromQuery
    $etherscanLikeAccountTokenTransactionOrderByAscQuery
    $etherscanLikeAccountTokenTransactionLimitQuery
    )
    UNION
    (
    $etherscanLikeAccountTokenTransactionSelectQuery
    $etherscanLikeAccountTokenTransactionWhereToQuery
    $etherscanLikeAccountTokenTransactionOrderByAscQuery
    $etherscanLikeAccountTokenTransactionLimitQuery
    )
    ) T1 
    ORDER BY T1.blockNumber ASC, T1.displayOrder ASC
    LIMIT :offset, :limit 
    """
const val etherscanLikeAccountTokenTransactionBlockNumberBetweenOrderByDescQuery = """
    SELECT T1.* FROM (
    (
    $etherscanLikeAccountTokenTransactionSelectQuery
    $etherscanLikeAccountTokenTransactionWhereFromQuery
    $etherscanLikeAccountTokenTransactionBlockNumberBetweenQuery
    $etherscanLikeAccountTokenTransactionOrderByDescQuery
    $etherscanLikeAccountTokenTransactionLimitQuery
    )
    UNION
    (
    $etherscanLikeAccountTokenTransactionSelectQuery
    $etherscanLikeAccountTokenTransactionWhereToQuery
    $etherscanLikeAccountTokenTransactionBlockNumberBetweenQuery
    $etherscanLikeAccountTokenTransactionOrderByDescQuery
    $etherscanLikeAccountTokenTransactionLimitQuery
    )
    ) T1 
    ORDER BY T1.blockNumber DESC, T1.displayOrder DESC
    LIMIT :offset, :limit
    """
const val etherscanLikeAccountTokenTransactionBlockNumberBetweenOrderByAscQuery = """
    SELECT T1.* FROM (
    (
    $etherscanLikeAccountTokenTransactionSelectQuery
    $etherscanLikeAccountTokenTransactionWhereFromQuery
    $etherscanLikeAccountTokenTransactionBlockNumberBetweenQuery
    $etherscanLikeAccountTokenTransactionOrderByAscQuery
    $etherscanLikeAccountTokenTransactionLimitQuery
    )
    UNION
    (
    $etherscanLikeAccountTokenTransactionSelectQuery
    $etherscanLikeAccountTokenTransactionWhereToQuery
    $etherscanLikeAccountTokenTransactionBlockNumberBetweenQuery
    $etherscanLikeAccountTokenTransactionOrderByAscQuery
    $etherscanLikeAccountTokenTransactionLimitQuery
    )
    ) T1 
    ORDER BY T1.blockNumber ASC, T1.displayOrder ASC
    LIMIT :offset, :limit
    """


const val etherscanLikeAccountTokenTransactionQueryWithContractAddressOrderDescQuery: String = """
    SELECT T1.* FROM (
    (
    $etherscanLikeAccountTokenTransactionSelectQuery
    $etherscanLikeAccountTokenTransactionWhereFromQuery
    $etherscanLikeAccountTokenTransactionWhereAndContractAddressQuery
    $etherscanLikeAccountTokenTransactionOrderByDescQuery
    $etherscanLikeAccountTokenTransactionLimitQuery
    )
    UNION
    (
    $etherscanLikeAccountTokenTransactionSelectQuery
    $etherscanLikeAccountTokenTransactionWhereToQuery
    $etherscanLikeAccountTokenTransactionWhereAndContractAddressQuery
    $etherscanLikeAccountTokenTransactionOrderByDescQuery
    $etherscanLikeAccountTokenTransactionLimitQuery
    )
    ) T1 
    ORDER BY T1.blockNumber DESC, T1.displayOrder DESC
    LIMIT :offset, :limit
    """
const val etherscanLikeAccountTokenTransactionQueryWithContractAddressOrderAscQuery: String = """
    SELECT T1.* FROM (
    (
    $etherscanLikeAccountTokenTransactionSelectQuery
    $etherscanLikeAccountTokenTransactionWhereFromQuery
    $etherscanLikeAccountTokenTransactionWhereAndContractAddressQuery
    $etherscanLikeAccountTokenTransactionOrderByAscQuery
    $etherscanLikeAccountTokenTransactionLimitQuery
    )
    UNION
    (
    $etherscanLikeAccountTokenTransactionSelectQuery
    $etherscanLikeAccountTokenTransactionWhereToQuery
    $etherscanLikeAccountTokenTransactionWhereAndContractAddressQuery
    $etherscanLikeAccountTokenTransactionOrderByAscQuery
    $etherscanLikeAccountTokenTransactionLimitQuery
    )
    ) T1 
    ORDER BY T1.blockNumber ASC, T1.displayOrder ASC
    LIMIT :offset, :limit 
    """
const val etherscanLikeAccountTokenTransactionWithContractAddressBlockNumberBetweenOrderByDescQuery = """
    SELECT T1.* FROM (
    (
    $etherscanLikeAccountTokenTransactionSelectQuery
    $etherscanLikeAccountTokenTransactionWhereFromQuery
    $etherscanLikeAccountTokenTransactionWhereAndContractAddressQuery
    $etherscanLikeAccountTokenTransactionBlockNumberBetweenQuery
    $etherscanLikeAccountTokenTransactionOrderByDescQuery
    $etherscanLikeAccountTokenTransactionLimitQuery
    )
    UNION
    (
    $etherscanLikeAccountTokenTransactionSelectQuery
    $etherscanLikeAccountTokenTransactionWhereToQuery
    $etherscanLikeAccountTokenTransactionWhereAndContractAddressQuery
    $etherscanLikeAccountTokenTransactionBlockNumberBetweenQuery
    $etherscanLikeAccountTokenTransactionOrderByDescQuery
    $etherscanLikeAccountTokenTransactionLimitQuery
    )
    ) T1 
    ORDER BY T1.blockNumber DESC, T1.displayOrder DESC
    LIMIT :offset, :limit
    """
const val etherscanLikeAccountTokenTransactionWithContractAddressBlockNumberBetweenOrderByAscQuery = """
    SELECT T1.* FROM (
    (
    $etherscanLikeAccountTokenTransactionSelectQuery
    $etherscanLikeAccountTokenTransactionWhereFromQuery
    $etherscanLikeAccountTokenTransactionWhereAndContractAddressQuery
    $etherscanLikeAccountTokenTransactionBlockNumberBetweenQuery
    $etherscanLikeAccountTokenTransactionOrderByAscQuery
    $etherscanLikeAccountTokenTransactionLimitQuery
    )
    UNION
    (
    $etherscanLikeAccountTokenTransactionSelectQuery
    $etherscanLikeAccountTokenTransactionWhereToQuery
    $etherscanLikeAccountTokenTransactionWhereAndContractAddressQuery
    $etherscanLikeAccountTokenTransactionBlockNumberBetweenQuery
    $etherscanLikeAccountTokenTransactionOrderByAscQuery
    $etherscanLikeAccountTokenTransactionLimitQuery
    )
    ) T1 
    ORDER BY T1.blockNumber ASC, T1.displayOrder ASC
    LIMIT :offset, :limit
    """


@Repository
interface TokenTransactionRepository : BaseRepository<TokenTransfer> {

    @Query(
        nativeQuery = true,
        value = etherscanLikeAccountTokenTransactionQueryOrderDescQuery
    )
    fun findAllByAccountAddressEtherscanLikeDesc(
        @Param("accountAddress") accountAddress: String,
        @Param("maxTotalCount") maxTotalCount: Long,
        @Param("offset") offset: Long,
        @Param("limit") limit: Long,
    ): List<List<String>>

    @Query(
        nativeQuery = true,
        value = etherscanLikeAccountTokenTransactionQueryOrderAscQuery
    )
    fun findAllByAccountAddressEtherscanLikeAsc(
        @Param("accountAddress") accountAddress: String,
        @Param("maxTotalCount") maxTotalCount: Long,
        @Param("offset") offset: Long,
        @Param("limit") limit: Long,
    ): List<List<String>>

    @Query(
        nativeQuery = true,
        value = etherscanLikeAccountTokenTransactionBlockNumberBetweenOrderByDescQuery
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
        value = etherscanLikeAccountTokenTransactionBlockNumberBetweenOrderByAscQuery
    )
    fun findAllByAccountAddressBlockNumberBetweenEtherscanLikeAsc(
        @Param("accountAddress") accountAddress: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        @Param("maxTotalCount") maxTotalCount: Long,
        @Param("offset") offset: Long,
        @Param("limit") limit: Long,
    ): List<List<String>>

    @Query(
        nativeQuery = true,
        value = etherscanLikeAccountTokenTransactionQueryWithContractAddressOrderDescQuery
    )
    fun findAllByAccountAddressWithContractAddressEtherscanLikeDesc(
        @Param("accountAddress") accountAddress: String,
        @Param("contractAddress") contractAddress: String,
        @Param("maxTotalCount") maxTotalCount: Long,
        @Param("offset") offset: Long,
        @Param("limit") limit: Long,
    ): List<List<String>>

    @Query(
        nativeQuery = true,
        value = etherscanLikeAccountTokenTransactionQueryWithContractAddressOrderAscQuery
    )
    fun findAllByAccountAddressWithContractAddressEtherscanLikeAsc(
        @Param("accountAddress") accountAddress: String,
        @Param("contractAddress") contractAddress: String,
        @Param("maxTotalCount") maxTotalCount: Long,
        @Param("offset") offset: Long,
        @Param("limit") limit: Long,
    ): List<List<String>>

    @Query(
        nativeQuery = true,
        value = etherscanLikeAccountTokenTransactionWithContractAddressBlockNumberBetweenOrderByDescQuery
    )
    fun findAllByAccountAddressWithContractAddressBlockNumberBetweenEtherscanLikeDesc(
        @Param("accountAddress") accountAddress: String,
        @Param("contractAddress") contractAddress: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        @Param("maxTotalCount") maxTotalCount: Long,
        @Param("offset") offset: Long,
        @Param("limit") limit: Long,
    ): List<List<String>>

    @Query(
        nativeQuery = true,
        value = etherscanLikeAccountTokenTransactionWithContractAddressBlockNumberBetweenOrderByAscQuery
    )
    fun findAllByAccountAddressWithContractAddressBlockNumberBetweenEtherscanLikeAsc(
        @Param("accountAddress") accountAddress: String,
        @Param("contractAddress") contractAddress: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        @Param("maxTotalCount") maxTotalCount: Long,
        @Param("offset") offset: Long,
        @Param("limit") limit: Long,
    ): List<List<String>>





}

