package io.klaytn.finder.domain.mysql.set2.index

import io.klaytn.finder.domain.mysql.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "internal_transaction_index")
data class InternalTransactionIndex(
    @Column
    val internalTxId: String,

    @Column
    val accountAddress: String,

    @Column
    val blockNumber: Long,

    @Column
    val transactionIndex: Int,

    @Column
    val callId: Int,
) : BaseEntity()