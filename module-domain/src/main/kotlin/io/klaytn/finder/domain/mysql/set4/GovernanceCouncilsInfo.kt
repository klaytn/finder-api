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
    var squareLink: String,

    @Column
    var name: String,

    @Column
    var thumbnail: String,

    @Column
    var website: String,

    @Column(columnDefinition = "TEXT")
    var summary: String?,

    @Column(columnDefinition = "TEXT")
    var description: String,

    @Column
    var apy: String,

    @Column
    var totalStaking: String,

    @Column(columnDefinition = "TINYINT")
    var isFoundation: Boolean,

    @Column
    var joinedAt: LocalDateTime?,

    @Column
    var activatedAt: LocalDateTime?,

    @Column
    var deactivatedAt: LocalDateTime?
): BaseEntity() {
    companion object {
        fun of(
            squareId: Long,
            squareLink: String,
            name: String,
            thumbnail: String,
            website: String,
            summary: String?,
            description: String,
            apy: String,
            totalStaking: String,
            isFoundation: Boolean,
            joinedAt: LocalDateTime
        ) = GovernanceCouncilsInfo(
            squareId = squareId,
            squareLink = squareLink,
            name = name,
            thumbnail = thumbnail,
            website = website,
            summary = summary,
            description = description,
            apy = apy,
            totalStaking = totalStaking,
            isFoundation = isFoundation,
            joinedAt = joinedAt,
            activatedAt = null,
            deactivatedAt = null
        )
    }
}
