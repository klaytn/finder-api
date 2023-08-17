package io.klaytn.finder.interfaces.rest.api.view.model.transaction

import io.klaytn.finder.view.model.account.AccountAddressView
import io.klaytn.finder.view.model.transaction.TransactionTypeView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.*

@Schema
data class TransactionListView(
    @Schema(title="Transaction Hash")
    val transactionHash: String,

    @Schema(title="Block #")
    val blockId: Long,

    @Schema(title="Transaction Creation Timestamp")
    val datetime: Date,

    @Schema(title="Address (from)")
    val from: AccountAddressView,

    @Schema(title="Address (to)")
    val to: AccountAddressView?,

    @Schema(title="Transaction Type")
    val transactionType: TransactionTypeView,

    @Schema(title="KLAY Amount")
    val amount: BigDecimal,

    @Schema(title="Transaction Fee")
    val transactionFee: BigDecimal,

    @Schema(title="Success Status")
    val success: Boolean,

    @Schema(title="Error Message in Case of Failure")
    val failMessage: String?,

    @Schema(title="Function Bytes")
    val methodId: String?,

    @Schema(title="Function Name")
    val signature: String?,

    @Schema(title="Effective Gas Price. Same as Block's baseFee")
    val effectiveGasPrice: BigDecimal,

    @Schema(title="Burnt Fees within Transaction Fee")
    val burntFees: BigDecimal?,
)
