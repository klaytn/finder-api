package io.klaytn.finder.view.model

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema
data class BlockRewardView(
        @Schema(title = "The amount minted") val minted: BigDecimal,
        @Schema(title = "Total tx fee spent)") val totalFee: BigDecimal,
        @Schema(title = "The amount burnt") val burntFee: BigDecimal,
        @Schema(title = "A mapping from reward type to reward amounts")
        val distributions: List<BlockRewardDistribution>,
        @Schema(title = "A mapping from reward recipient addresses to reward amounts")
        val recipients: List<BlockRewardTarget>
)

@Schema
data class BlockRewardTarget(
        val type: BlockRewordAddressType,
        val address: String,
        val amount: BigDecimal
)

@Schema
data class BlockRewardDistribution(val type: BlockRewordAddressType, val amount: BigDecimal)

enum class BlockRewordAddressType(val order: Int) {
    PROPOSER(0),
    KGF(1),
    KIR(2),
    STAKER(3),
    STAKERS(3)
}
