package io.klaytn.finder.domain.mysql.set1.approve

import io.klaytn.finder.domain.common.AccountAddress
import io.klaytn.finder.domain.common.AccountAddressAttributeConverter
import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.domain.mysql.BaseEntity
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "account_nft_approves")
data class AccountNftApprove(
    @Column
    val blockNumber: Long,

    @Column
    val transactionHash: String,

    @Column
    val accountAddress: String,

    @Column
    @Convert(converter = AccountAddressAttributeConverter::class)
    val spenderAddress: AccountAddress,

    @Column(columnDefinition = "TINYINT")
    val contractType: ContractType,

    @Column
    val contractAddress: String,

    @Column(columnDefinition = "TINYINT")
    var approvedAll: Boolean,

    @Column(columnDefinition = "TEXT")
    var approvedTokenId: String?,

    @Column
    val timestamp: Int,
) : BaseEntity()
