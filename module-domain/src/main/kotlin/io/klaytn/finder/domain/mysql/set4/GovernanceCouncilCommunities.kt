package io.klaytn.finder.domain.mysql.set4

import io.klaytn.finder.domain.mysql.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "governance_council_communities")
data class GovernanceCouncilCommunities (
    @Column
    val communityId: Long,

    @Column
    val name: String,

    @Column
    val squareId: Long,

    @Column
    val links: String,

    @Column
    val thumbnail: String,
) : BaseEntity() {
    companion object {
        fun of(
            communityId: Long,
            name: String,
            squareId: Long,
            links: String,
            thumbnail: String,
        ) = GovernanceCouncilCommunities(
            communityId = communityId,
            name = name,
            squareId = squareId,
            links = links,
            thumbnail = thumbnail,
        )
    }
}