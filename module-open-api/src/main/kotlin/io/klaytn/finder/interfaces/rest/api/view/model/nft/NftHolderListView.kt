package io.klaytn.finder.interfaces.rest.api.view.model.nft

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.math.BigInteger

@Schema
data class NftHolderListView(
        @Schema(title = "Holder") val holder: String,
        @Schema(title = "Quantity") val tokenCount: BigInteger,
        @Schema(title = "Percentage") val percentage: BigDecimal? = null,
        @Schema(title = "Token ID") val tokenId: String? = null,
)
