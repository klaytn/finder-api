package io.klaytn.finder.interfaces.rest.api.view.model.nft

import io.klaytn.finder.domain.common.ContractType
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema
data class NftView(
    @Schema(title = "Contract Type")
    val contractType: ContractType?,

    @Schema(title = "NFT Name")
    val name: String?,

    @Schema(title = "NFT Symbol")
    val symbol: String?,

    @Schema(title = "NFT Image")
    val icon: String?,

    @Schema(title = "Total NFT Supply")
    val totalSupply: BigDecimal,

    @Schema(title = "Total Transfers")
    val totalTransfers: Long,

    @Schema(title = "Number of Holders")
    val holderCount: Long,

    @Schema(title = "Official Site URL")
    val officialSite: String?,
)
