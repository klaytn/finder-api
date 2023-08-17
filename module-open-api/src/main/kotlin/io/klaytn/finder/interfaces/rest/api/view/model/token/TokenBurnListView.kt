package io.klaytn.finder.interfaces.rest.api.view.model.token

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.*

@Schema
data class TokenBurnListView(
        @Schema(title = "Block #") val blockId: Long,
        @Schema(title = "Transaction Hash") val transactionHash: String,
        @Schema(title = "Transaction DateTime") val datetime: Date,
        @Schema(title = "Address (from)") val from: String,
        @Schema(title = "Address (to)") val to: String?,
        @Schema(title = "Token Transfer Amount") val amount: BigDecimal,
)
