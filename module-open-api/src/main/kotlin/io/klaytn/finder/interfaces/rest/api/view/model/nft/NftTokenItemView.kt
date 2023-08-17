package io.klaytn.finder.interfaces.rest.api.view.model.nft

import io.klaytn.finder.interfaces.rest.api.view.model.ContractSummary
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigInteger

@Schema
data class NftTokenItemView(
    @Schema(title = "Contract Information")
    val contract: ContractSummary,

    @Schema(title = "NFT Token ID")
    val tokenId: String,

    @Schema(title = "Token URI")
    val tokenUri: String,

    /**
     * Applicable to KIP-17
     */
    @Schema(title = "(only for KIP-17) Holder Address")
    val holder: String?,

    /**
     * Applicable to KIP-37
     */
    @Schema(title = "(only for KIP-37) Total Supply of Token ID")
    val totalSupply: BigInteger?,
    @Schema(title = "(only for KIP-37) Total Transfers of Token ID")
    val totalTransfer: Long?,

    @Schema(title = "Token URI Refreshable Status")
    val tokenUriRefreshable: Boolean?,
)
