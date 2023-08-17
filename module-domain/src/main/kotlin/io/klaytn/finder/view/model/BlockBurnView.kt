package io.klaytn.finder.view.model

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema
data class BlockBurnView(
        @Schema(title = "The nearest block number with burn information") val nearestBlockNumber: Long,
        @Schema(title = "Total burnt amount (auto burn, fees) up to the current block") val accumulateBurntFees: BigDecimal,
        @Schema(title = "Total burnt KLAY amount up to the current block (manual burn)") val accumulateBurntKlay: BigDecimal,
        @Schema(title = "Burn amount due to KIP103 (KORE) hard fork") val kip103Burnt: BigDecimal = BigDecimal("5296324269.908256422492837664"),
) {
    fun getAccumulateBurnt() = accumulateBurntFees.plus(accumulateBurntKlay).plus(kip103Burnt)
}
