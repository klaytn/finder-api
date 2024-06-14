package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.mysql.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "kaia_user_login_history")
data class KaiaUserLoginHistory (
    @Column
    val userId: Long,

    @Column
    val timestamp: Int,
) : BaseEntity()