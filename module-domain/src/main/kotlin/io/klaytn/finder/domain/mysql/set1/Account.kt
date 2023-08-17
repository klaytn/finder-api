package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.common.AccountType
import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.domain.mysql.BaseEntity
import io.klaytn.finder.infra.db.jpa.JsonStringToStringListConverter
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "accounts")
data class Account(
    @Column
    val address: String,

    @Column(columnDefinition = "TINYINT")
    val accountType: AccountType,

    @Column
    val balance: BigDecimal,

    @Column
    val totalTransactionCount: Long,

    @Column(columnDefinition = "TINYINT")
    val contractType: ContractType,

    @Column
    val contractCreatorAddress: String?,

    @Column
    val contractCreatorTransactionHash: String?,

    @Column
    var knsDomain: String?,

    @Column
    var addressLabel: String?,

    @Column(columnDefinition = "TEXT")
    @Convert(converter = JsonStringToStringListConverter::class)
    var tags: List<String>?,

    @Column
    val contractDeployerAddress: String?,
    ) : BaseEntity() {
    companion object {
        fun of(
            address: String,
            accountType: AccountType,
            balance: BigDecimal,
            totalTransactionCount: Long,
            contractType: ContractType,
        ) =
            Account(
                address = address,
                accountType = accountType,
                balance = balance,
                totalTransactionCount = totalTransactionCount,
                contractType = contractType,
                contractCreatorAddress = null,
                contractCreatorTransactionHash = null,
                contractDeployerAddress = null,
                knsDomain = null,
                addressLabel = null,
                tags = null,
            )
    }
}
