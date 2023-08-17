package io.klaytn.finder.domain.mysql.set1.governancecouncil

import io.klaytn.finder.domain.mysql.BaseRepository
import org.springframework.stereotype.Repository

@Repository
interface GovernanceCouncilRepository : BaseRepository<GovernanceCouncil> {
    fun findBySquareId(squareId: Long): GovernanceCouncil?
}