package io.klaytn.finder.domain.mysql.set2

import io.klaytn.finder.domain.common.AccountAddress
import io.klaytn.finder.domain.common.AccountAddressAttributeConverter
import io.klaytn.finder.domain.mysql.BaseEntity
import io.klaytn.finder.infra.db.jpa.PebToKlayConverter
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "internal_transactions")
class InternalTransaction(
    @Column
    val internalTxId: String,

    @Column
    val blockNumber: Long,

    @Column
    val callId: Int,

    @Column
    val parentCallId: Int?,

    @Column
    val transactionIndex: Int,

    @Column
    val type: String,

    @Column
    @Convert(converter = AccountAddressAttributeConverter::class)
    val from: AccountAddress,

    @Column
    @Convert(converter = AccountAddressAttributeConverter::class)
    val to: AccountAddress?,

    @Column
    @Convert(converter = PebToKlayConverter::class)
    val value: BigDecimal = BigDecimal.ZERO,

    @Column
    val gas: Long?,

    @Column
    val gasUsed: Long?,

    @Column(columnDefinition = "TEXT")
    val input: String,

    @Column(columnDefinition = "TEXT")
    val output: String?,

    @Column
    val time: String?,

    @Column(columnDefinition = "TEXT")
    val error: String?,

    @Column(columnDefinition = "TEXT")
    val reverted: String?,
) : BaseEntity() {
    fun getMethodId() =
        if (input.length >= 10) input.substring(0, 10) else null
}

