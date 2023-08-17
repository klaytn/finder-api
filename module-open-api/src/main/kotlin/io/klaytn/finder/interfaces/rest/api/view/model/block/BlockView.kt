package io.klaytn.finder.interfaces.rest.api.view.model.block

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.Date

@Schema
data class BlockView(
        @Schema(title = "Block #") val blockId: Long,
        @Schema(title = "Block Creation Time (UTC)") val datetime: Date,
        @Schema(title = "HASH") val hash: String,
        @Schema(title = "Parent HASH") val parentHash: String,
        @Schema(title = "Number of Transactions in the Block") val totalTransactionCount: Long,
        @Schema(title = "Block Reward") val blockReward: BlockRewardView,
        @Schema(title = "Block Size (bytes)") val blockSize: Long,
        @Schema(title = "Committee") val blockCommittee: BlockCommitteeView,
        @Schema(title = "Block Gas Cost") val baseFeePerGas: BigDecimal,
)

@Schema
data class BlockRewardView(
        @Schema(title = "Minted KLAY") val minted: BigDecimal,
        @Schema(title = "Total Fee") val totalFee: BigDecimal?,
        @Schema(title = "Total Burnt Fee") val burntFee: BigDecimal?,
)

@Schema
data class BlockCommitteeView(
        @Schema(title = "Block Proposer") val blockProposer: String,
        @Schema(title = "Validators") val validators: List<String>,
)
