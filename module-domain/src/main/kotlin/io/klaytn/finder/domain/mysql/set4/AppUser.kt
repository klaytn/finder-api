package io.klaytn.finder.domain.mysql.set4

import io.klaytn.finder.domain.mysql.BaseEntity
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "app_users")
data class AppUser(
    @Column
    val emailAddress: String,

    @Column
    val appPricePlanId: Long,

    @Column
    val activatedAt: LocalDateTime?,

    @Column
    val deactivatedAt: LocalDateTime?,
) : BaseEntity()
