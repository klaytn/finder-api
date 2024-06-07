package io.klaytn.finder.domain.mysql.set4

import io.klaytn.finder.domain.mysql.BaseEntity
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "governance_council_contracts")
data class GovernanceCouncilContracts (
    @Column
    val squareId: Long,

    @Column
    val address: String,

    @Column(columnDefinition = "TINYINT")
    val addressType: Int,

    @Column(columnDefinition = "TINYINT")
    val version: Int,
) : BaseEntity()
