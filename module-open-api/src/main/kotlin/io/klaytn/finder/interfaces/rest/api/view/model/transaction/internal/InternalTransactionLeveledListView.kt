package io.klaytn.finder.interfaces.rest.api.view.model.transaction.internal

import io.klaytn.finder.interfaces.rest.api.view.model.transaction.TransactionInputDataView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema
data class InternalTransactionLeveledListView(
    @Schema(title="Call ID")
    val callId: Int,

    @Schema(title="Parent Call ID")
    val parentCallId: Int?,

    @Schema(title="Level")
    var level: Int,

    @Schema(title="Type", example = "CALL,CREATE")
    val type: String,

    @Schema(title="Address (from)")
    val from: String,

    @Schema(title="Address (to)")
    val to: String?,

    @Schema(title="KLAY Amount Transferred")
    val amount: BigDecimal,

    @Schema(title="Gas Limit")
    val gasLimit: Long?,

    @Schema(title="INPUT")
    val inputData: TransactionInputDataView?,

    @Schema(title="OUTPUT")
    val outputData: String?,

    @Schema(title="Error Information")
    val error: String?,

    @Schema(title="Reverted Information")
    val reverted: RevertedInfo?,
)

@Schema
data class RevertedInfo(
    @Schema(title="Contract Address")
    val contract: String,

    @Schema(title="Message")
    val message: String?,
)
