package io.klaytn.finder.interfaces.rest.api.view

import io.klaytn.finder.domain.common.ContractType
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema
data class ContractItemView(
    @Schema(title="Contract Address")
    val address: String,

    @Schema(title="Type")
    val type: ContractType,

    @Schema(title="Name")
    val name: String?,

    @Schema(title="Symbol")
    val symbol: String?,

    @Schema(title="Decimal")
    val decimal: Int,

    @Schema(title="Total Supply")
    val totalSupply: BigDecimal,

    @Schema(title="Token Image")
    val icon: String?,

    @Schema(title="Official Site URL")
    val officialSite: String?,

    @Schema(title="Official Email Address")
    val officialEmailAddress: String?,

    @Schema(title="Verification Status")
    val verified: Boolean,
)
