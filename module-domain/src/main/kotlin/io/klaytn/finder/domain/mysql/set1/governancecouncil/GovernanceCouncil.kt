package io.klaytn.finder.domain.mysql.set1.governancecouncil

import io.klaytn.finder.domain.mysql.BaseEntity
import io.klaytn.finder.infra.db.jpa.JsonStringToStringListConverter
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "governance_councils")
data class GovernanceCouncil(
    @Column
    val squareId: Long,

    @Column
    var squareLink: String,

    @Column
    var name: String,

    @Column
    var thumbnail: String,

    @Column
    @Convert(converter = JsonStringToStringListConverter::class)
    var website: List<String>,

    @Column
    var joinedAt: LocalDateTime?,
) : BaseEntity() {
    companion object {
        fun of(squareId: Long, squareLink: String, name: String, thumbnail: String, website: List<String>, joinedAt: LocalDateTime) =
            GovernanceCouncil(
                squareId = squareId,
                squareLink = squareLink,
                name = name,
                thumbnail = thumbnail,
                website = website,
                joinedAt = joinedAt
            )
    }
}