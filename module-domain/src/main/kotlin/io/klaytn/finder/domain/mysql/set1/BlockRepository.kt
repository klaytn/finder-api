package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.mysql.BaseRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BlockRepository : BaseRepository<Block> {
    fun findAllBy(pageable: Pageable): List<BlockNumber>

    fun findAllByNumberBetween(blockNumberStart: Long, blockNumberEnd: Long, pageable: Pageable): List<BlockNumber>

    fun findTop1ByTimestampLessThanEqualOrderByTimestampDescNumberDesc(timestamp: Int): BlockNumber?

    @Query(
        nativeQuery = true,
        value = """
            SELECT 
                sum(transaction_count)
            FROM 
                blocks
            WHERE 
                `timestamp` between :timestampStart and :timestampEnd
        """
    )
    fun findTotalTransactionCountTimestampBetween(
        @Param("timestampStart") timestampStart: Long,
        @Param("timestampEnd") timestampEnd: Long
    ): Long?

    @Query(
        nativeQuery = true,
        value = """
            SELECT 
                number 
            FROM 
                blocks force index(ix_proposer_blocknumber_timestamp)
            WHERE 
                `proposer` = :proposer   
            ORDER BY
                number desc
        """
    )
    fun findAllByProposer(@Param("proposer") proposer: String, pageable: Pageable): List<BlockNumber>

    @Query(
        nativeQuery = true,
        value = """
            SELECT 
                number 
            FROM 
                blocks force index(ix_proposer_blocknumber_timestamp)
            WHERE 
                `proposer` = :proposer AND
                number between :blockNumberStart and :blockNumberEnd
            ORDER BY
                number desc
        """
    )
    fun findAllByProposerAndBlockNumberBetween(
        @Param("proposer") proposer: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        pageable: Pageable
    ): List<BlockNumber>

    fun <T> findAllByProposerAndDateAndNumberLessThan(
        type: Class<T>,
        proposer: String, date: String, number: Long, pageable: Pageable,
    ): List<T>

    fun findAllByNumberIn(numbers: List<Long>): List<Block>

    @Query(
        nativeQuery = true,
        value = """
            SELECT number FROM blocks ORDER BY number desc LIMIT 1        
        """
    )
    fun findLatestNumber(): Long?

    @Query(
        nativeQuery = true,
        value = """
            SELECT count(*) FROM (
                SELECT 1 FROM blocks limit :maxTotalCount
            ) t
        """
    )
    fun countAll(@Param("maxTotalCount") maxTotalCount: Long): Long

    @Query(
        nativeQuery = true,
        value = """
            SELECT count(*) FROM (
                SELECT 1 FROM blocks WHERE proposer = :proposer limit :maxTotalCount
            ) t
        """
    )
    fun countAllByProposer(
        @Param("proposer") proposer: String,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long

    @Query(
        nativeQuery = true,
        value = """
            SELECT count(*) FROM (
                SELECT 
                    1 
                FROM 
                    blocks 
                WHERE 
                    proposer = :proposer AND
                    number between :blockNumberStart and :blockNumberEnd
                limit 
                    :maxTotalCount
            ) t
        """
    )
    fun countAllByProposerAndBlockNumberBetween(
        @Param("proposer") proposer: String,
        @Param("blockNumberStart") blockNumberStart: Long,
        @Param("blockNumberEnd") blockNumberEnd: Long,
        @Param("maxTotalCount") maxTotalCount: Long,
    ): Long
}


interface ProposerBlock {
    val number: Long
    val timestamp: Int
    val transactionCount: Long
    val gasUsed: Int
    val size: Long
    val baseFeePerGas: String?
}

interface BlockNumber {
    val number: Long
}