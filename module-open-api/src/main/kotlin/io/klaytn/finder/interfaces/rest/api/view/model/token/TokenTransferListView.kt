package io.klaytn.finder.interfaces.rest.api.view.model.token

import io.klaytn.finder.interfaces.rest.api.view.model.ContractSummary
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.*

@Schema
data class TokenTransferListView(
    @Schema(title="Contract Information")
    val contract: ContractSummary,

    @Schema(title = "Block #")
    val blockId: Long,

    @Schema(title = "Transaction Hash")
    val transactionHash: String,

    @Schema(title = "Transaction Index")
    val transactionIndex: Int?,

    @Schema(title = "Transaction Time")
    val datetime: Date,

    @Schema(title = "Address (From)")
    val from: String,

    @Schema(title = "Address (To)")
    val to: String?,

    @Schema(title = "Token Transfer Amount")
    val amount: BigDecimal,
)
