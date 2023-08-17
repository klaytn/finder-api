package io.klaytn.finder.interfaces.rest.api.view.model.nft

import io.klaytn.finder.interfaces.rest.api.view.model.contract.ContractSummary
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

data class NftListView(
    @Schema(title="NFT Information")
    val info: ContractSummary,

    @Schema(title="Total Token Count")
    val totalSupply: BigDecimal,

    @Schema(title="Total Token Transfers")
    val totalTransfers: Long,
)
