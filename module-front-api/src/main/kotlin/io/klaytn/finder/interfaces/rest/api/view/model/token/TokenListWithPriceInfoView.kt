package io.klaytn.finder.interfaces.rest.api.view.model.token

import io.klaytn.finder.interfaces.rest.api.view.model.contract.ContractSummary
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema
data class TokenListWithPriceInfoView (
    @Schema(title="Token Information")
    val info: ContractSummary,

    @Schema(title="Total Token Supply")
    val totalSupply: BigDecimal,

    @Schema(title="Total Transfers")
    val totalTransfers: Long,

    @Schema(title="Total Token Burns")
    val totalBurns: Long,

    @Schema(title="Token Burn Amount")
    val burnAmount: BigDecimal,

    @Schema(title="Token Price info")
    val priceInfo: TokenPriceInfoView
)