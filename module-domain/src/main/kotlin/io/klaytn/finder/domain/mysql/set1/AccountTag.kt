package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.mysql.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "account_tags")
data class AccountTag(
    @Column
    val tag: String,

    @Column(columnDefinition = "TINYINT")
    var tagOrder: Int,

    @Column(columnDefinition = "TINYINT")
    var display: Boolean
) : BaseEntity()