package io.klaytn.finder.domain.mysql.set1

import com.klaytn.caver.transaction.type.TransactionType
import io.klaytn.finder.domain.common.AccountAddress
import io.klaytn.finder.domain.common.AccountAddressAttributeConverter
import io.klaytn.finder.domain.mysql.BaseEntity
import io.klaytn.finder.infra.db.DbTableConstants
import io.klaytn.finder.infra.db.jpa.JsonStringToMapListConverter
import io.klaytn.finder.infra.db.jpa.PebToKlayConverter
import java.math.BigDecimal
import java.math.BigInteger
import javax.persistence.*

@Entity
@Table(name = DbTableConstants.transactions)
data class Transaction(
    @Column
    val transactionHash: String,

    @Column
    val blockNumber: Long,

    @Column
    val blockHash: String,

    @Column
    val transactionIndex: Int,

    @Column
    val codeFormat: String?,

    /**
     * created contract
     */
    @Column
    val contractAddress: String?,

    @Column
    val feePayer: String?,

    @Column(columnDefinition = "TEXT")
    @Convert(converter = JsonStringToMapListConverter::class)
    val feePayerSignatures: List<Map<String, String>>?,

    @Column
    val feeRatio: String?,

    @Column
    @Convert(converter = AccountAddressAttributeConverter::class)
    val from: AccountAddress,

    @Column
    @Convert(converter = AccountAddressAttributeConverter::class)
    val to: AccountAddress?,

    @Column(columnDefinition = "bigint")
    val gas: BigInteger,

    @Column
    @Convert(converter = PebToKlayConverter::class)
    val gasPrice: BigDecimal,

    @Column(columnDefinition = "bigint")
    val gasUsed: BigInteger,

    @Column(columnDefinition = "TINYINT")
    val humanReadable: Boolean? = false,

    @Column(columnDefinition = "TEXT")
    val key: String?,

    @Column(columnDefinition = "TEXT")
    val input: String?,

    @Column(columnDefinition = "TEXT")
    val logsBloom: String,

    @Column
    val nonce: Long,

    @Column
    val senderTxHash: String?,

    @Column(columnDefinition = "TEXT")
    @Convert(converter = JsonStringToMapListConverter::class)
    val signatures: List<Map<String, String>>?,

    @Column(columnDefinition = "TINYINT")
    val status: Int,

    @Column
    val txError: Int?,

    @Column
    @Enumerated(EnumType.STRING)
    val type: TransactionType,

    @Column
    val typeInt: Int,

    @Column
    @Convert(converter = PebToKlayConverter::class)
    val value: BigDecimal = BigDecimal.ZERO,

    @Column
    val tokenTransferCount: Int,

    @Column
    val nftTransferCount: Int,

    @Column
    val timestamp: Int,

    @Column(columnDefinition = "TEXT")
    val accessList: String?,

    @Column
    val chainId: String?,

    @Column
    @Convert(converter = PebToKlayConverter::class)
    val maxFeePerGas: BigDecimal?,

    @Column
    @Convert(converter = PebToKlayConverter::class)
    val maxPriorityFeePerGas: BigDecimal?,

    /**
     * Same value as block#base_fee_per_gas.
     */
    @Column
    @Convert(converter = PebToKlayConverter::class)
    val effectiveGasPrice: BigDecimal?,
) : BaseEntity() {
    fun getMethodId() =
        input?.let { if (it.length >= 10) it.substring(0, 10) else null }
}
