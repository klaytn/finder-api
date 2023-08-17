package io.klaytn.finder.interfaces.rest.api.view.model.transaction

import io.klaytn.finder.view.model.account.AccountAddressView
import io.klaytn.finder.view.model.account.KlaytnAccountKeyView
import io.klaytn.finder.view.model.transaction.TransactionStatus
import io.klaytn.finder.view.model.transaction.TransactionTypeView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.*

@Schema
data class TransactionItemView(
    @Schema(title="Transaction Hash")
    val transactionHash: String,

    @Schema(title="Transaction Type")
    val transactionType: TransactionTypeView,

    @Schema(title="Block #")
    val blockId: Long,

    @Schema(title="Address (from)")
    val from: AccountAddressView,

    @Schema(title="Address (to)")
    val to: AccountAddressView?,

    @Schema(title="Token Transfer")
    val tokenTransfer: Int,

    @Schema(title="NFT Transfer")
    val nftTransfer: Int,

    @Schema(title="Fee Payer")
    val feePayer: String?,

    @Schema(title="Fee Payer Account")
    val feePayerAccount: AccountAddressView?,

    @Schema(title="Fee Ration")
    val feeRation: Int?,

    @Schema(title="Transaction Timestamp (UTC)")
    val datetime: Date,

    @Schema(title="Nonce")
    val nonce: Long,

    @Schema(title="KLAY Amount")
    val amount: BigDecimal,

    @Schema(title="Gas Price")
    val gasPrice: BigDecimal,

    @Schema(title="Gas Used")
    val gasUsed: BigDecimal,

    @Schema(title="Gas Limit")
    val gasLimit: BigDecimal,

    @Schema(title="Transaction Fee")
    val transactionFee: BigDecimal,

    @Schema(title="Transaction Status")
    val status: TransactionStatus,

    @Schema(title="Fail Message when Transaction Status is Fail")
    val failMessage: String?,

    @Schema(title="Created Contract Address within the Transaction")
    val createdContractAccount: AccountAddressView?,

    @Schema(title="Function Bytes")
    val methodId: String?,

    @Schema(title="Function Name")
    val signature: String?,

    @Schema(title="Effective Gas Price. Same as Block's baseFee")
    val effectiveGasPrice: BigDecimal,

    @Schema(title="Burnt Fees within Transaction Fee")
    val burntFees: BigDecimal?,

    @Schema(title="Account Key")
    val key: String?,

    @Schema(title="Account Key View")
    val accountKey: KlaytnAccountKeyView?
)
