package io.klaytn.finder.interfaces.rest.api.view.model.block

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.Date

@Schema
data class BlockListView(
        @Schema(title = "Block #") val blockId: Long,
        @Schema(title = "Block Creation DateTime") val datetime: Date,
        @Schema(title = "Number of Transactions in the Block") val totalTransactionCount: Long,
        @Schema(title = "Block Proposer from Committee") val blockProposer: String?,
        @Schema(title = "Reward KLAY") val reward: BigDecimal,
        @Schema(title = "Block Size (bytes)") val blockSize: Long,
        @Schema(title = "Block Gas Cost") val baseFeePerGas: BigDecimal,
        @Schema(title = "Burnt Fees") val burntFees: BigDecimal?,
)
