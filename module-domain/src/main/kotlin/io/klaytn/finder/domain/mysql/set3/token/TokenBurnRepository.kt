package io.klaytn.finder.domain.mysql.set3.token

import io.klaytn.finder.domain.mysql.BaseRepository
import io.klaytn.finder.domain.mysql.EntityId
import io.klaytn.finder.infra.db.DbTableConstants
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TokenBurnRepository : BaseRepository<TokenBurn> {
    @Query(
        nativeQuery = true,
        value = """
            SELECT 
                id 
            FROM 
                ${DbTableConstants.tokenBurns} force index(ix_contractaddress_displayorder)
            WHERE 
                contract_address = :contractAddress 
            ORDER BY 
                display_order DESC
        """
    )
    fun findAllByContractAddress(
        contractAddress: String,
        pageable: Pageable,
    ): List<EntityId>

    @Query(
        nativeQuery = true,
        value = """
            SELECT 
                id 
            FROM 
                ${DbTableConstants.tokenBurns} 
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

    // -----------------------------------------------------------------------------------------------------------------
    // -- count
    // -----------------------------------------------------------------------------------------------------------------

    @Query(
        nativeQuery = true,
        value = """
            SELECT count(*) FROM (
                SELECT 
                    1 
                FROM 
                    ${DbTableConstants.tokenBurns} 
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
                    ${DbTableConstants.tokenBurns} 
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
}
