package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.mysql.BaseRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BlockBurnRepository : BaseRepository<BlockBurn> {
    fun findFirstByNumberLessThanEqual(blockNumber: Long): BlockBurn?
}