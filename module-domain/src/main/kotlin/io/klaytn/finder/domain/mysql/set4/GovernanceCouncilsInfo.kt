package io.klaytn.finder.domain.mysql.set4

import io.klaytn.finder.domain.mysql.BaseEntity
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "governance_councils_info")
data class GovernanceCouncilsInfo (
    @Column
    val squareId: Long,

    @Column
    val squareLink: String,

    @Column
    val name: String,

    @Column
    val thumbnail: String,

    @Column
    val website: String,

    @Column(columnDefinition = "TEXT")
    val summary: String,

    @Column(columnDefinition = "TEXT")
    val description: String,

    @Column
    val apy: String,

    @Column
    val totalStaking: String,

    @Column(columnDefinition = "TINYINT")
    val isFoundation: Boolean,

    @Column
    val joinedAt: LocalDateTime?,

    @Column
    val activatedAt: LocalDateTime?,

    @Column
    val deactivatedAt: LocalDateTime?
): BaseEntity()
