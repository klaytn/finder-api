package io.klaytn.finder.domain.mysql.set3.nft

import io.klaytn.finder.domain.mysql.BaseRepository
import io.klaytn.finder.domain.mysql.EntityId
import io.klaytn.finder.infra.db.DbTableConstants
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface NftTransferRepository : BaseRepository<NftTransfer> {
    fun findAllByTransactionHash(transactionHash: String, pageable: Pageable): Page<EntityId>

    @Query(
        nativeQuery = true,
        value = """
            (
                (
                    SELECT 
                        id, block_number, display_order 
                    FROM 
                        ${DbTableConstants.nftTransfers} 
                    WHERE 
                        `from` = :accountAddress AND contract_address = :contractAddress 
                    ORDER BY 
                        block_number DESC, display_order DESC 
                    limit :maxTotalCount
                )
                union 
                (
                    SELECT 
                        id, block_number, display_order 
                    FROM 
                        ${DbTableConstants.nftTransfers} 
                    WHERE 
                        `to`   = :accountAddress AND contract_address = :contractAddress 
                    ORDER BY 
                        block_number DESC, display_order DESC 
                    limit :maxTotalCount
                )
            )
            ORDER BY block_number DESC, display_order DESC
            """,
    )
    fun findAllByAccountAddressAndContractAddress(
        @Param("accountAddress") accountAddress: String,
        @Param("contractAddress") contractAddress: String,
        @Param("maxTotalCount") maxTotalCount: Long,
        pageable: Pageable,
    ): List<EntityId>

    @Query(
        nativeQuery = true,
        value = """
            (
                (
                    SELECT 
                        id, block_number, display_order 
                    FROM 
                        ${DbTableConstants.nftTransfers} 
                    WHERE 
                        `from` = :accountAddress AND contract_address = :contractAddress AND
                        block_number between :blockNumberStart and :blockNumberEnd 
                    ORDER BY 
                        block_number DESC, display_order DESC 
                    limit :maxTotalCount
                )
                union 
                (
                    SELECT 
                        id, block_number, display_order 
                    FROM 
                        ${DbTableConstants.nftTransfers} 
                    WHERE 
                        `to`   = :accountAddress AND contract_address = :contractAddress AND
                        block_number between :blockNumberStart and :blockNumberEnd 
                    ORDER BY 
                        block_number DESC, display_order DESC 
                    limit :maxTotalCount
                )
            )
            ORDER BY block_number DESC, display_order DESC
            """,
    )
    fun findAllByAccountAddressAndContractAddressAndBlockNumberBetween(
        @Param("accountAddress") accountAddress: String,
        @Param("contractAddress") contractAddress: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        @Param("maxTotalCount") maxTotalCount: Long,
        pageable: Pageable,
    ): List<EntityId>

    @Query(
        nativeQuery = true,
        value = """
            (
                (
                    SELECT 
                        id, block_number, display_order 
                    FROM 
                        ${DbTableConstants.nftTransfers} 
                    WHERE 
                        `from` = :accountAddress 
                    ORDER BY 
                        block_number DESC, display_order DESC 
                    limit :maxTotalCount
                )
                union 
                (
                    SELECT 
                        id, block_number, display_order 
                    FROM 
                        ${DbTableConstants.nftTransfers} 
                    WHERE 
                        `to`   = :accountAddress 
                    ORDER BY 
                        block_number DESC, display_order DESC 
                    limit :maxTotalCount
                )
            )
            ORDER BY block_number DESC, display_order DESC
            """,
    )
    fun findAllByAccountAddress(
        @Param("accountAddress") accountAddress: String,
        @Param("maxTotalCount") maxTotalCount: Long,
        pageable: Pageable,
    ): List<EntityId>

    @Query(
        nativeQuery = true,
        value = """
            (
                (
                    SELECT 
                        id, block_number, display_order 
                    FROM 
                        ${DbTableConstants.nftTransfers} 
                    WHERE 
                        `from` = :accountAddress AND
                        block_number between :blockNumberStart and :blockNumberEnd 
                    ORDER BY 
                        block_number DESC, display_order DESC 
                    limit :maxTotalCount
                )
                union 
                (
                    SELECT 
                        id, block_number, display_order 
                    FROM 
                        ${DbTableConstants.nftTransfers} 
                    WHERE 
                        `to` = :accountAddress AND
                        block_number between :blockNumberStart and :blockNumberEnd 
                    ORDER BY 
                        block_number DESC, display_order DESC 
                    limit :maxTotalCount
                )
            )
            ORDER BY block_number DESC, display_order DESC
            """,
    )
    fun findAllByAccountAddressAndBlockNumberBetween(
        @Param("accountAddress") accountAddress: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        @Param("maxTotalCount") maxTotalCount: Long,
        pageable: Pageable,
    ): List<EntityId>

    @Query(
        nativeQuery = true,
        value = """
            SELECT 
                id 
            FROM 
                ${DbTableConstants.nftTransfers} 
            WHERE 
                contract_address = :contractAddress 
            ORDER BY 
                block_number DESC, display_order DESC
        """
    )
    fun findAllByContractAddress(
        @Param("contractAddress") contractAddress: String,
        pageable: Pageable,
    ): List<EntityId>

    @Query(
        nativeQuery = true,
        value = """
            SELECT 
                id 
            FROM 
                ${DbTableConstants.nftTransfers} 
            WHERE 
                contract_address = :contractAddress AND
                block_number between :blockNumberStart and :blockNumberEnd
            ORDER BY 
                block_number DESC, display_order DESC
        """
    )
    fun findAllByContractAddressAndBlockNumberBetween(
        @Param("contractAddress") contractAddress: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        pageable: Pageable,
    ): List<EntityId>

    @Query(
        nativeQuery = true,
        value = """
            SELECT 
                id 
            FROM 
                ${DbTableConstants.nftTransfers} 
            WHERE 
                contract_address = :contractAddress AND token_id = :tokenId 
            ORDER BY 
                display_order DESC
        """
    )
    fun findAllByContractAddressAndTokenId(
        @Param("contractAddress") contractAddress: String,
        @Param("tokenId") tokenId: String,
        pageable: Pageable,
    ): List<EntityId>

    // -----------------------------------------------------------------------------------------------------------------
    // -- count
    // -----------------------------------------------------------------------------------------------------------------

    @Query(
        nativeQuery = true,
        value = """
            SELECT SUM(cnt) as cnt FROM (
                (
                    SELECT count(*) as CNT FROM (
                        SELECT 
                            1 
                        FROM 
                            ${DbTableConstants.nftTransfers} 
                        WHERE 
                            `from` = :accountAddress AND contract_address = :contractAddress 
                        limit :maxTotalCount
                    ) t1
                )
                union all
                (
                    SELECT count(*) as CNT FROM (
                        SELECT 
                            1 
                        FROM 
                            ${DbTableConstants.nftTransfers} 
                        WHERE 
                            `to`   = :accountAddress AND contract_address = :contractAddress 
                        limit :maxTotalCount
                    ) t2
                )
            ) as s
            """,
    )
    fun countAllByAccountAddressAndContractAddress(
        @Param("accountAddress") accountAddress: String,
        @Param("contractAddress") contractAddress: String,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long

    @Query(
        nativeQuery = true,
        value = """
            SELECT SUM(cnt) as cnt FROM (
                (
                    SELECT count(*) as CNT FROM (
                        SELECT 
                            1 
                        FROM 
                            ${DbTableConstants.nftTransfers} 
                        WHERE 
                            `from` = :accountAddress AND contract_address = :contractAddress AND
                            block_number between :blockNumberStart and :blockNumberEnd
                        limit :maxTotalCount
                    ) t1
                )
                union all
                (
                    SELECT count(*) as CNT FROM (
                        SELECT 
                            1 
                        FROM 
                            ${DbTableConstants.nftTransfers} 
                        WHERE 
                            `to`   = :accountAddress AND contract_address = :contractAddress AND
                            block_number between :blockNumberStart and :blockNumberEnd
                        limit :maxTotalCount
                    ) t2
                )
            ) as s
            """,
    )
    fun countAllByAccountAddressAndContractAddressAndBlockNumberBetween(
        @Param("accountAddress") accountAddress: String,
        @Param("contractAddress") contractAddress: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long

    @Query(
        nativeQuery = true,
        value = """
            SELECT SUM(cnt) as cnt FROM (
                (
                    SELECT count(*) as CNT FROM (
                        SELECT 
                            1 
                        FROM 
                            ${DbTableConstants.nftTransfers} 
                        WHERE 
                            `from` = :accountAddress 
                        limit :maxTotalCount
                    ) t1
                )
                union all
                (
                    SELECT count(*) as CNT FROM (
                        SELECT 
                            1 
                        FROM 
                            ${DbTableConstants.nftTransfers} 
                        WHERE 
                            `to`   = :accountAddress 
                        limit :maxTotalCount
                    ) t2
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
            SELECT SUM(cnt) as cnt FROM (
                (
                    SELECT count(*) as CNT FROM (
                        SELECT 
                            1 
                        FROM 
                            ${DbTableConstants.nftTransfers} 
                        WHERE 
                            `from` = :accountAddress AND
                            block_number between :blockNumberStart and :blockNumberEnd
                        limit :maxTotalCount
                    ) t1
                )
                union all
                (
                    SELECT count(*) as CNT FROM (
                        SELECT 
                            1 
                        FROM 
                            ${DbTableConstants.nftTransfers} 
                        WHERE 
                            `to`   = :accountAddress AND
                            block_number between :blockNumberStart and :blockNumberEnd
                        limit :maxTotalCount
                    ) t2
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
            SELECT count(*) FROM (
                SELECT 
                    1 
                FROM 
                    ${DbTableConstants.nftTransfers} 
                WHERE 
                    contract_address = :contractAddress 
                limit :maxTotalCount
            ) t
        """
    )
    fun countAllByContractAddress(
        @Param("contractAddress") contractAddress: String,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long

    @Query(
        nativeQuery = true,
        value = """
            SELECT count(*) FROM (
                SELECT 
                    1 
                FROM 
                    ${DbTableConstants.nftTransfers} 
                WHERE 
                    contract_address = :contractAddress AND
                    block_number between :blockNumberStart and :blockNumberEnd
                limit :maxTotalCount
            ) t
        """
    )
    fun countAllByContractAddressAndBlockNumberBetween(
        @Param("contractAddress") contractAddress: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long

    @Query(
        nativeQuery = true,
        value = """
            SELECT count(*) FROM (
                SELECT 
                    1 
                FROM 
                    ${DbTableConstants.nftTransfers} 
                WHERE 
                    contract_address = :contractAddress AND token_id = :tokenId 
                limit :maxTotalCount
            ) t
        """
    )
    fun countAllByContractAddressAndTokenId(
        @Param("contractAddress") contractAddress: String,
        @Param("tokenId") tokenId: String,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long

    @Query(
        nativeQuery = true,
        value = """
            SELECT
                CASE WHEN EXISTS (
                (
                    (select id from ${DbTableConstants.nftTransfers} where `from` = :accountAddress limit 1)
                    union
                    (select id from ${DbTableConstants.nftTransfers} where `to` = :accountAddress limit 1)
                )
            )
            THEN 'true'
            ELSE 'false'
            END            
        """
    )
    fun existsByAccountAddress(@Param("accountAddress") accountAddress: String): Boolean
}
