package io.klaytn.finder.domain.mysql.set3

import io.klaytn.finder.domain.mysql.BaseRepository
import io.klaytn.finder.infra.db.DbTableConstants
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import io.klaytn.finder.domain.mysql.set3.nft.NftTransfer

const val etherscanLikeAccountNFTTransactionSelectQuery: String = """
    SELECT
        t.transaction_hash AS `hash`,
        CAST(t.block_number AS CHAR) AS blockNumber,
        CAST(t.timestamp AS CHAR) AS timeStamp,
        t.from,
        t.to,
        t.contract_address AS contractAddress,
        t.token_count AS tokenValue,
        t.token_id AS tokenID,
        t.contract_type AS contractType,
        t.display_order AS displayOrder
        FROM
        ${DbTableConstants.nftTransfers} t
    """
const val etherscanLikeAccountNFTTransactionWhereFromQuery: String = "WHERE t.from = :accountAddress"
const val etherscanLikeAccountNFTTransactionWhereToQuery: String = "WHERE t.to = :accountAddress"
const val etherscanLikeAccountNFTTransactionWhereAndContractAddressQuery: String = "AND t.contract_address = :contractAddress"
const val etherscanLikeAccountNFTTransactionWhereInAndContractTypeQuery: String = "AND t.contract_type IN :contractTypes"
const val etherscanLikeAccountNFTTransactionBlockNumberBetweenQuery: String = " AND t.block_number BETWEEN :blockNumberStart AND :blockNumberEnd "
const val etherscanLikeAccountNFTTransactionOrderByDescQuery: String = "ORDER BY t.block_number DESC, t.display_order DESC"
const val etherscanLikeAccountNFTTransactionOrderByAscQuery: String = "ORDER BY t.block_number ASC, t.display_order ASC"
const val etherscanLikeAccountNFTTransactionLimitQuery: String = "LIMIT :maxTotalCount"

const val etherscanLikeAccountNFTTransactionQueryOrderDescQuery: String = """
    SELECT T1.* FROM (
    (
    $etherscanLikeAccountNFTTransactionSelectQuery
    $etherscanLikeAccountNFTTransactionWhereFromQuery
    $etherscanLikeAccountNFTTransactionWhereInAndContractTypeQuery
    $etherscanLikeAccountNFTTransactionOrderByDescQuery
    $etherscanLikeAccountNFTTransactionLimitQuery
    )
    UNION
    (
    $etherscanLikeAccountNFTTransactionSelectQuery
    $etherscanLikeAccountNFTTransactionWhereToQuery
    $etherscanLikeAccountNFTTransactionWhereInAndContractTypeQuery
    $etherscanLikeAccountNFTTransactionOrderByDescQuery
    $etherscanLikeAccountNFTTransactionLimitQuery
    )
    ) T1 
    ORDER BY T1.blockNumber DESC, T1.displayOrder DESC
    LIMIT :offset, :limit
    """
const val etherscanLikeAccountNFTTransactionQueryOrderAscQuery: String = """
    SELECT T1.* FROM (
    (
    $etherscanLikeAccountNFTTransactionSelectQuery
    $etherscanLikeAccountNFTTransactionWhereFromQuery
    $etherscanLikeAccountNFTTransactionWhereInAndContractTypeQuery
    $etherscanLikeAccountNFTTransactionOrderByAscQuery
    $etherscanLikeAccountNFTTransactionLimitQuery
    )
    UNION
    (
    $etherscanLikeAccountNFTTransactionSelectQuery
    $etherscanLikeAccountNFTTransactionWhereToQuery
    $etherscanLikeAccountNFTTransactionWhereInAndContractTypeQuery
    $etherscanLikeAccountNFTTransactionOrderByAscQuery
    $etherscanLikeAccountNFTTransactionLimitQuery
    )
    ) T1 
    ORDER BY T1.blockNumber ASC, T1.displayOrder ASC
    LIMIT :offset, :limit 
    """
const val etherscanLikeAccountNFTTransactionBlockNumberBetweenOrderByDescQuery = """
    SELECT T1.* FROM (
    (
    $etherscanLikeAccountNFTTransactionSelectQuery
    $etherscanLikeAccountNFTTransactionWhereFromQuery
    $etherscanLikeAccountNFTTransactionWhereInAndContractTypeQuery
    $etherscanLikeAccountNFTTransactionBlockNumberBetweenQuery
    $etherscanLikeAccountNFTTransactionOrderByDescQuery
    $etherscanLikeAccountNFTTransactionLimitQuery
    )
    UNION
    (
    $etherscanLikeAccountNFTTransactionSelectQuery
    $etherscanLikeAccountNFTTransactionWhereToQuery
    $etherscanLikeAccountNFTTransactionWhereInAndContractTypeQuery
    $etherscanLikeAccountNFTTransactionBlockNumberBetweenQuery
    $etherscanLikeAccountNFTTransactionOrderByDescQuery
    $etherscanLikeAccountNFTTransactionLimitQuery
    )
    ) T1 
    ORDER BY T1.blockNumber DESC, T1.displayOrder DESC
    LIMIT :offset, :limit
    """
const val etherscanLikeAccountNFTTransactionBlockNumberBetweenOrderByAscQuery = """
    SELECT T1.* FROM (
    (
    $etherscanLikeAccountNFTTransactionSelectQuery
    $etherscanLikeAccountNFTTransactionWhereFromQuery
    $etherscanLikeAccountNFTTransactionWhereInAndContractTypeQuery
    $etherscanLikeAccountNFTTransactionBlockNumberBetweenQuery
    $etherscanLikeAccountNFTTransactionOrderByAscQuery
    $etherscanLikeAccountNFTTransactionLimitQuery
    )
    UNION
    (
    $etherscanLikeAccountNFTTransactionSelectQuery
    $etherscanLikeAccountNFTTransactionWhereToQuery
    $etherscanLikeAccountNFTTransactionWhereInAndContractTypeQuery
    $etherscanLikeAccountNFTTransactionBlockNumberBetweenQuery
    $etherscanLikeAccountNFTTransactionOrderByAscQuery
    $etherscanLikeAccountNFTTransactionLimitQuery
    )
    ) T1 
    ORDER BY T1.blockNumber ASC, T1.displayOrder ASC
    LIMIT :offset, :limit
    """


const val etherscanLikeAccountNFTTransactionQueryWithContractAddressOrderDescQuery: String = """
    SELECT T1.* FROM (
    (
    $etherscanLikeAccountNFTTransactionSelectQuery
    $etherscanLikeAccountNFTTransactionWhereFromQuery
    $etherscanLikeAccountNFTTransactionWhereInAndContractTypeQuery
    $etherscanLikeAccountNFTTransactionWhereAndContractAddressQuery
    $etherscanLikeAccountNFTTransactionOrderByDescQuery
    $etherscanLikeAccountNFTTransactionLimitQuery
    )
    UNION
    (
    $etherscanLikeAccountNFTTransactionSelectQuery
    $etherscanLikeAccountNFTTransactionWhereToQuery
    $etherscanLikeAccountNFTTransactionWhereInAndContractTypeQuery
    $etherscanLikeAccountNFTTransactionWhereAndContractAddressQuery
    $etherscanLikeAccountNFTTransactionOrderByDescQuery
    $etherscanLikeAccountNFTTransactionLimitQuery
    )
    ) T1 
    ORDER BY T1.blockNumber DESC, T1.displayOrder DESC
    LIMIT :offset, :limit
    """
const val etherscanLikeAccountNFTTransactionQueryWithContractAddressOrderAscQuery: String = """
    SELECT T1.* FROM (
    (
    $etherscanLikeAccountNFTTransactionSelectQuery
    $etherscanLikeAccountNFTTransactionWhereFromQuery
    $etherscanLikeAccountNFTTransactionWhereInAndContractTypeQuery
    $etherscanLikeAccountNFTTransactionWhereAndContractAddressQuery
    $etherscanLikeAccountNFTTransactionOrderByAscQuery
    $etherscanLikeAccountNFTTransactionLimitQuery
    )
    UNION
    (
    $etherscanLikeAccountNFTTransactionSelectQuery
    $etherscanLikeAccountNFTTransactionWhereToQuery
    $etherscanLikeAccountNFTTransactionWhereInAndContractTypeQuery
    $etherscanLikeAccountNFTTransactionWhereAndContractAddressQuery
    $etherscanLikeAccountNFTTransactionOrderByAscQuery
    $etherscanLikeAccountNFTTransactionLimitQuery
    )
    ) T1 
    ORDER BY T1.blockNumber ASC, T1.displayOrder ASC
    LIMIT :offset, :limit 
    """
const val etherscanLikeAccountNFTTransactionWithContractAddressBlockNumberBetweenOrderByDescQuery = """
    SELECT T1.* FROM (
    (
    $etherscanLikeAccountNFTTransactionSelectQuery
    $etherscanLikeAccountNFTTransactionWhereFromQuery
    $etherscanLikeAccountNFTTransactionWhereInAndContractTypeQuery
    $etherscanLikeAccountNFTTransactionWhereAndContractAddressQuery
    $etherscanLikeAccountNFTTransactionBlockNumberBetweenQuery
    $etherscanLikeAccountNFTTransactionOrderByDescQuery
    $etherscanLikeAccountNFTTransactionLimitQuery
    )
    UNION
    (
    $etherscanLikeAccountNFTTransactionSelectQuery
    $etherscanLikeAccountNFTTransactionWhereToQuery
    $etherscanLikeAccountNFTTransactionWhereInAndContractTypeQuery
    $etherscanLikeAccountNFTTransactionWhereAndContractAddressQuery
    $etherscanLikeAccountNFTTransactionBlockNumberBetweenQuery
    $etherscanLikeAccountNFTTransactionOrderByDescQuery
    $etherscanLikeAccountNFTTransactionLimitQuery
    )
    ) T1 
    ORDER BY T1.blockNumber DESC, T1.displayOrder DESC
    LIMIT :offset, :limit
    """
const val etherscanLikeAccountNFTTransactionWithContractAddressBlockNumberBetweenOrderByAscQuery = """
    SELECT T1.* FROM (
    (
    $etherscanLikeAccountNFTTransactionSelectQuery
    $etherscanLikeAccountNFTTransactionWhereFromQuery
    $etherscanLikeAccountNFTTransactionWhereInAndContractTypeQuery
    $etherscanLikeAccountNFTTransactionWhereAndContractAddressQuery
    $etherscanLikeAccountNFTTransactionBlockNumberBetweenQuery
    $etherscanLikeAccountNFTTransactionOrderByAscQuery
    $etherscanLikeAccountNFTTransactionLimitQuery
    )
    UNION
    (
    $etherscanLikeAccountNFTTransactionSelectQuery
    $etherscanLikeAccountNFTTransactionWhereToQuery
    $etherscanLikeAccountNFTTransactionWhereInAndContractTypeQuery
    $etherscanLikeAccountNFTTransactionWhereAndContractAddressQuery
    $etherscanLikeAccountNFTTransactionBlockNumberBetweenQuery
    $etherscanLikeAccountNFTTransactionOrderByAscQuery
    $etherscanLikeAccountNFTTransactionLimitQuery
    )
    ) T1 
    ORDER BY T1.blockNumber ASC, T1.displayOrder ASC
    LIMIT :offset, :limit
    """


@Repository
interface NFTTransactionRepository : BaseRepository<NftTransfer> {

    @Query(
        nativeQuery = true,
        value = etherscanLikeAccountNFTTransactionQueryOrderDescQuery
    )
    fun findAllByAccountAddressEtherscanLikeDesc(
        @Param("accountAddress") accountAddress: String,
        @Param("contractTypes") contractTypes: List<Int>,
        @Param("maxTotalCount") maxTotalCount: Long,
        @Param("offset") offset: Long,
        @Param("limit") limit: Long,
    ): List<List<String>>

    @Query(
        nativeQuery = true,
        value = etherscanLikeAccountNFTTransactionQueryOrderAscQuery
    )
    fun findAllByAccountAddressEtherscanLikeAsc(
        @Param("accountAddress") accountAddress: String,
        @Param("contractTypes") contractTypes: List<Int>,
        @Param("maxTotalCount") maxTotalCount: Long,
        @Param("offset") offset: Long,
        @Param("limit") limit: Long,
    ): List<List<String>>

    @Query(
        nativeQuery = true,
        value = etherscanLikeAccountNFTTransactionBlockNumberBetweenOrderByDescQuery
    )
    fun findAllByAccountAddressBlockNumberBetweenEtherscanLikeDesc(
        @Param("accountAddress") accountAddress: String,
        @Param("contractTypes") contractTypes: List<Int>,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        @Param("maxTotalCount") maxTotalCount: Long,
        @Param("offset") offset: Long,
        @Param("limit") limit: Long,
    ):List<List<String>>


    @Query(
        nativeQuery = true,
        value = etherscanLikeAccountNFTTransactionBlockNumberBetweenOrderByAscQuery
    )
    fun findAllByAccountAddressBlockNumberBetweenEtherscanLikeAsc(
        @Param("accountAddress") accountAddress: String,
        @Param("contractTypes") contractTypes: List<Int>,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        @Param("maxTotalCount") maxTotalCount: Long,
        @Param("offset") offset: Long,
        @Param("limit") limit: Long,
    ): List<List<String>>

    @Query(
        nativeQuery = true,
        value = etherscanLikeAccountNFTTransactionQueryWithContractAddressOrderDescQuery
    )
    fun findAllByAccountAddressWithContractAddressEtherscanLikeDesc(
        @Param("accountAddress") accountAddress: String,
        @Param("contractTypes") contractTypes: List<Int>,
        @Param("contractAddress") contractAddress: String,
        @Param("maxTotalCount") maxTotalCount: Long,
        @Param("offset") offset: Long,
        @Param("limit") limit: Long,
    ): List<List<String>>

    @Query(
        nativeQuery = true,
        value = etherscanLikeAccountNFTTransactionQueryWithContractAddressOrderAscQuery
    )
    fun findAllByAccountAddressWithContractAddressEtherscanLikeAsc(
        @Param("accountAddress") accountAddress: String,
        @Param("contractTypes") contractTypes: List<Int>,
        @Param("contractAddress") contractAddress: String,
        @Param("maxTotalCount") maxTotalCount: Long,
        @Param("offset") offset: Long,
        @Param("limit") limit: Long,
    ): List<List<String>>

    @Query(
        nativeQuery = true,
        value = etherscanLikeAccountNFTTransactionWithContractAddressBlockNumberBetweenOrderByDescQuery
    )
    fun findAllByAccountAddressWithContractAddressBlockNumberBetweenEtherscanLikeDesc(
        @Param("accountAddress") accountAddress: String,
        @Param("contractTypes") contractTypes: List<Int>,
        @Param("contractAddress") contractAddress: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        @Param("maxTotalCount") maxTotalCount: Long,
        @Param("offset") offset: Long,
        @Param("limit") limit: Long,
    ): List<List<String>>

    @Query(
        nativeQuery = true,
        value = etherscanLikeAccountNFTTransactionWithContractAddressBlockNumberBetweenOrderByAscQuery
    )
    fun findAllByAccountAddressWithContractAddressBlockNumberBetweenEtherscanLikeAsc(
        @Param("accountAddress") accountAddress: String,
        @Param("contractTypes") contractTypes: List<Int>,
        @Param("contractAddress") contractAddress: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        @Param("maxTotalCount") maxTotalCount: Long,
        @Param("offset") offset: Long,
        @Param("limit") limit: Long,
    ): List<List<String>>





}

