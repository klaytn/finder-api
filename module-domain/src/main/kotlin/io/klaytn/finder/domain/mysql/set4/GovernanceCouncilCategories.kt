package io.klaytn.finder.domain.mysql.set4

import io.klaytn.finder.domain.mysql.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table


@Entity
@Table(name = "governance_council_categories")
data class GovernanceCouncilCategories (
    @Column
    val categoryId: Long,

    @Column
    val categoryName: String,

    @Column
    val squareId: Long,
) : BaseEntity()