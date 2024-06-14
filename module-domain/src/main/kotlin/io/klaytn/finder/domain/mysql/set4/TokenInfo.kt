package io.klaytn.finder.domain.mysql.set4

import io.klaytn.finder.domain.mysql.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "token_info")
data class TokenInfo(
    @Column
    val cmcId: Int,

    @Column
    val name: String,

    @Column
    val symbol: String,

    @Column
    val contractAddress: String,

    @Column
    var isActive: Int,
) : BaseEntity()