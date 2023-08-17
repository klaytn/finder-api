package io.klaytn.finder.domain.mysql.set3

import io.klaytn.finder.domain.mysql.BaseRepository
import io.klaytn.finder.domain.mysql.EntityId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface EventLogRepository : BaseRepository<EventLog> {
    fun findAllByTransactionHash(transactionHash: String, pageable: Pageable): Page<EntityId>
    fun findAllByTransactionHashAndSignature(
        transactionHash: String, signature: String, pageable: Pageable): Page<EntityId>

    fun findAllByAddress(address: String, pageable: Pageable): List<EntityId>
    fun findAllByAddressAndSignature(address: String, signature: String, pageable: Pageable): List<EntityId>

    fun findAllByAddressAndBlockNumberBetween(
        address: String, blockNumberStart: Long, blockNumberEnd: Long, pageable: Pageable): List<EntityId>
    fun findAllByAddressAndSignatureAndBlockNumberBetween(
        address: String, signature: String, blockNumberStart: Long, blockNumberEnd: Long, pageable: Pageable): List<EntityId>

    @Query(
        nativeQuery = true,
        value = """
            SELECT count(*) FROM (
                SELECT 
                    1 
                FROM 
                    event_logs 
                WHERE 
                    address = :address 
                limit :maxTotalCount
            ) t
        """
    )
    fun countAllByAddress(
        @Param("address") address: String,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long

    @Query(
        nativeQuery = true,
        value = """
            SELECT count(*) FROM (
                SELECT 
                    1 
                FROM 
                    event_logs 
                WHERE 
                    address = :address AND
                    signature = :signature
                limit :maxTotalCount
            ) t
        """
    )
    fun countAllByAddressAndSignature(
        @Param("address") address: String,
        @Param("signature") signature: String,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long

    @Query(
        nativeQuery = true,
        value = """
            SELECT count(*) FROM (
                SELECT 
                    1 
                FROM 
                    event_logs 
                WHERE 
                    address = :address AND
                    block_number between :blockNumberStart and :blockNumberEnd
                limit :maxTotalCount
            ) t
        """
    )
    fun countAllByAddressAndBlockNumberBetween(
        @Param("address") address: String,
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
                    event_logs 
                WHERE 
                    address = :address AND
                    signature = :signature AND
                    block_number between :blockNumberStart and :blockNumberEnd
                limit :maxTotalCount
            ) t
        """
    )
    fun countAllByAddressAndSignatureAndBlockNumberBetween(
        @Param("address") address: String,
        @Param("signature") signature: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long

    fun existsByAddress(address: String): Boolean
}
