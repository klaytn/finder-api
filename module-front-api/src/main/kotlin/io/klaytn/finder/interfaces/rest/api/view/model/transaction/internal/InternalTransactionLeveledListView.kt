package io.klaytn.finder.interfaces.rest.api.view.model.transaction.internal

import io.klaytn.finder.view.model.account.AccountAddressView
import io.klaytn.finder.interfaces.rest.api.view.model.transaction.input.InputDataView
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
    val from: AccountAddressView,

    @Schema(title="Address (to)")
    val to: AccountAddressView?,

    @Schema(title="KLAY Amount")
    val amount: BigDecimal,

    @Schema(title="Gas Limit")
    val gasLimit: Long?,

    @Schema(title="Input Data")
    val inputData: InputDataView?,

    @Schema(title="Output Data")
    val outputData: String?,

    @Schema(title="Error Information")
    val error: String?,

    @Schema(title="Revert Information")
    val reverted: RevertedInfo?,
)

@Schema
data class RevertedInfo(
    @Schema(title="Contract Address")
    val contract: String,

    @Schema(title="Message")
    val message: String?,
)
