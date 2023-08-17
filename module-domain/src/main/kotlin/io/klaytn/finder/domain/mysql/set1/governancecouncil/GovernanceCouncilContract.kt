package io.klaytn.finder.domain.mysql.set1.governancecouncil

import io.klaytn.finder.domain.common.GovernanceCouncilContractType
import io.klaytn.finder.domain.mysql.BaseEntity
import javax.persistence.*

@Entity
@Table(name = "governance_council_contracts")
data class GovernanceCouncilContract(
    @Column
    val squareId: Long,

    @Column
    val address: String,

    @Column(columnDefinition = "TINYINT")
    val addressType: GovernanceCouncilContractType,
) : BaseEntity()