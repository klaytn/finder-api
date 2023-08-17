package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.common.TransferType
import io.klaytn.finder.domain.mysql.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "account_transfer_contracts")
data class AccountTransferContract(
    @Column
    val accountAddress: String,

    @Column
    val contractAddress: String,

    @Column(columnDefinition = "TINYINT")
    val transferType: TransferType,
) : BaseEntity()
