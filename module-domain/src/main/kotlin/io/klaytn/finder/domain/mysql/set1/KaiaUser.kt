package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.common.KaiaUserType
import io.klaytn.finder.domain.mysql.BaseEntity
import javax.persistence.*

@Entity
@Table(name = "kaia_users")
data class KaiaUser(
    @Column
    val name: String,

    @Column
    val email: String,

    @Column
    val password: String,

    @Column
    val profileImage: String?,

    @Column(columnDefinition = "TINYINT")
    val isSubscribed: Boolean,

    @field:Enumerated(value = EnumType.ORDINAL)
    @field:Column(columnDefinition = "TINYINT")
    var status: KaiaUserType,

    @Column
    val registerTimestamp: Int
) : BaseEntity()