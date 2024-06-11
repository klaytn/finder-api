package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.common.KaiaUserType
import io.klaytn.finder.domain.mysql.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "kaia_users")
data class KaiaUser (
    @Column
    val name: String,

    @Column
    val email: String,

    @Column
    val password: String,

    @Column
    val profileImage: String,

    @Column(columnDefinition = "TINYINT")
    val isSubscribed: Boolean,

    @Column(columnDefinition = "TINYINT")
    val status: KaiaUserType,

    @Column
    val registerTimestamp: Int
): BaseEntity()