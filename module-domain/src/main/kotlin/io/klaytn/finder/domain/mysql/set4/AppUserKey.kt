package io.klaytn.finder.domain.mysql.set4

import io.klaytn.finder.domain.mysql.BaseEntity
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "app_user_keys")
data class AppUserKey(
    @Column
    val appUserId: Long,

    @Column
    val accessKey: String,

    @Column
    val name: String,

    @Column(columnDefinition = "TEXT")
    val description: String?,

    @Column
    val activatedAt: LocalDateTime?,

    @Column
    val deactivatedAt: LocalDateTime?,
) : BaseEntity()
