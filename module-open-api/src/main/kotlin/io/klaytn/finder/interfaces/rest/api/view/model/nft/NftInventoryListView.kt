package io.klaytn.finder.interfaces.rest.api.view.model.nft

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigInteger
import java.util.*

@Schema
data class NftInventoryListView(
        @Schema(title = "Token ID") val tokenId: String,
        @Schema(title = "Holder") val holder: String,
        @Schema(title = "Token URI") val tokenUri: String,
        @Schema(title = "Token Count") val tokenCount: BigInteger? = null,
        @Schema(title = "Token Update Time (UTC)") val updatedAt: Date,
)
