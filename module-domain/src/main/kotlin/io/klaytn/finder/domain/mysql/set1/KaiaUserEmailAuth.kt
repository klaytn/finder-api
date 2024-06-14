package io.klaytn.finder.domain.mysql.set1

import com.fasterxml.jackson.annotation.JsonProperty
import io.klaytn.finder.domain.common.KaiaUserEmailAuthType
import io.klaytn.finder.domain.mysql.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "kaia_user_email_auth")
data class KaiaUserEmailAuth (
    @Column
    val email: String,

    @Column
    val userId: Long,

    @Column
    val authType: KaiaUserEmailAuthType,

    @Column
    val jwtToken: String,

    @Column(columnDefinition = "TINYINT")
    val isVerified: Boolean,
): BaseEntity()
