package io.klaytn.finder.interfaces.rest.api.view.model.token

import io.klaytn.finder.domain.common.ContractType
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema
data class TokenView(
    @Schema(title = "Contract Type")
    val contractType: ContractType?,

    @Schema(title="Token Name")
    val name: String?,

    @Schema(title="Token Symbol")
    val symbol: String?,

    @Schema(title="Token Image")
    val icon: String?,

    @Schema(title="Token Decimal")
    val decimal: Int,

    @Schema(title = "Total Token Supply")
    val totalSupply: BigDecimal,

    @Schema(title = "Total Transfers")
    val totalTransfers: Long,

    @Schema(title = "Official Site URL")
    val officialSite: String?,

    @Schema(title = "Token Burn Amount")
    val burnAmount: BigDecimal?,

    @Schema(title = "Total Token Burns")
    val totalBurns: Long?,
)
