package io.klaytn.finder.interfaces.rest.api.view.model.nft

import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.interfaces.rest.api.view.model.contract.ContractSummary
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema
data class NftItemView(
    @Schema(title="NFT Information")
    val info: ContractSummary,

    @Schema(title="NFT Type")
    val type: ContractType,

    @Schema(title="Total NFT Count")
    val totalSupply: BigDecimal,

    @Schema(title="Total Transfers")
    val totalTransfers: Long,

    @Schema(title="Holder Count")
    val holderCount: Long,

    @Schema(title="Official Site URL")
    val officialSite: String?,

    @Schema(title="Token Burn Amount")
    val burnAmount: BigDecimal?,

    @Schema(title="Total Token Burns")
    val totalBurns: Long?,
)
