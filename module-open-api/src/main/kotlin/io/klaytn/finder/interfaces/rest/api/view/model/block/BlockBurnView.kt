package io.klaytn.finder.interfaces.rest.api.view.model.block

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema
data class BlockBurnView(
        @Schema(title = "Nearest Block Number with Burn Information") val nearestBlockNumber: Long,
        @Schema(title = "Total Burnt Amount (Auto Burn, Fees) up to Current Block") val accumulateBurntFees: BigDecimal,
        @Schema(title = "Total Burnt Amount (Manual Burn) up to Current Block") val accumulateBurntKlay: BigDecimal,
        @Schema(title = "Burn Amount due to KIP103(KORE) Hard Fork") val kip103Burnt: BigDecimal,
) {
    fun getAccumulateBurnt() = accumulateBurntFees.plus(accumulateBurntKlay).plus(kip103Burnt)
}
