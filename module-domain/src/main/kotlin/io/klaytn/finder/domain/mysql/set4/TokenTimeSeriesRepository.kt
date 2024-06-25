package io.klaytn.finder.domain.mysql.set4

import io.klaytn.finder.domain.mysql.BaseRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

interface TokenTimeSeriesRepository : BaseRepository<TokenTimeSeries> {
    @Query(
        value = """
        SELECT * FROM (
            SELECT t.*, ROW_NUMBER() OVER (PARTITION BY t.symbol ORDER BY t.timestamp DESC) as rn
            FROM token_time_series t
            WHERE t.symbol IN :symbols
        ) subquery
        WHERE subquery.rn = 1
    """, nativeQuery = true
    )
    fun findLatestBySymbols(@Param("symbols") symbols: List<String>): List<TokenTimeSeries>
}