package io.klaytn.finder.interfaces.rest.api.view.model.transaction.internal

import io.klaytn.finder.view.model.account.AccountAddressView
import io.klaytn.finder.view.model.transaction.TransactionStatusView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.*

@Schema
data class InternalTransactionListView(
    @Schema(title="Call ID")
    val callId: Int,

    @Schema(title="Block #")
    val blockId: Long,

    @Schema(title="Transaction Hash")
    val transactionHash: String?,

    @Schema(title="Transaction Timestamp (UTC)")
    val datetime: Date?,

    @Schema(title="Type", example = "CALL,CREATE")
    val type: String,

    @Schema(title="Address (from)")
    val from: AccountAddressView,

    @Schema(title="Address (to)")
    val to: AccountAddressView?,

    @Schema(title="KLAY Amount")
    val amount: BigDecimal,

    @Schema(title="Error Information")
    val error: String?,

    @Schema(title="Function Bytes")
    val methodId: String?,

    @Schema(title="Function Name")
    val signature: String?,

    @Schema(title="Transaction Status")
    val transactionStatus: TransactionStatusView?,
)
