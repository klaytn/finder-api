package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.mysql.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "account_keys")
data class AccountKey(
    @Column
    val blockNumber: Long,

    @Column
    val transactionHash: String,

    @Column
    val accountAddress: String,

    @Column(columnDefinition = "TEXT")
    var accountKey: String,
) : BaseEntity()

