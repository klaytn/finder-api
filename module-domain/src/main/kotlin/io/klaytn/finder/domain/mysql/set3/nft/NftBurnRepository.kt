package io.klaytn.finder.domain.mysql.set3.nft

import io.klaytn.finder.domain.mysql.BaseRepository
import io.klaytn.finder.domain.mysql.EntityId
import io.klaytn.finder.infra.db.DbTableConstants
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface NftBurnRepository : BaseRepository<NftBurn> {
    @Query(
        nativeQuery = true,
        value = """
            SELECT id FROM ${DbTableConstants.nftBurns} WHERE contract_address = :contractAddress ORDER BY display_order DESC
        """
    )
    fun findAllByContractAddress(
        contractAddress: String,
        pageable: Pageable,
    ): List<EntityId>

    @Query(
        nativeQuery = true,
        value = """
            SELECT id FROM ${DbTableConstants.nftBurns} WHERE contract_address = :contractAddress AND token_id = :tokenId ORDER BY display_order DESC
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
            SELECT count(*) FROM (
                SELECT 1 FROM ${DbTableConstants.nftBurns} WHERE contract_address = :contractAddress limit :maxTotalCount
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
                SELECT 1 FROM ${DbTableConstants.nftBurns} WHERE contract_address = :contractAddress AND token_id = :tokenId limit :maxTotalCount
            ) t
        """
    )
    fun countAllByContractAddressAndTokenId(
        @Param("contractAddress") contractAddress: String,
        @Param("tokenId") tokenId: String,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long
}
