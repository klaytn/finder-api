package io.klaytn.finder.domain.mysql.set1.governancecouncil

import io.klaytn.finder.domain.mysql.BaseRepository
import io.klaytn.finder.domain.mysql.EntityId
import org.springframework.stereotype.Repository

@Repository
interface GovernanceCouncilContractRepository : BaseRepository<GovernanceCouncilContract> {
    fun findAllBySquareId(squareId: Long): List<EntityId>
    fun findByAddress(address: String): EntityId?
}