package io.klaytn.finder.interfaces.rest.papi.view

import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.interfaces.rest.api.view.model.contract.ContractSummary
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigInteger

@Schema
data class PaiNftInventoryListView(
    @Schema(title="NFT Information")
    val nft: ContractSummary,

    @Schema(title="NFT Type")
    val nftType: ContractType,

    @Schema(title="Token ID")
    val tokenId: String,

    @Schema(title="Token URI")
    val tokenUri: String,

    @Schema(title="Token Count")
    val tokenCount: BigInteger? = null,
)
