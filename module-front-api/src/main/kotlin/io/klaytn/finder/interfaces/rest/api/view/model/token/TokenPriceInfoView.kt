package io.klaytn.finder.interfaces.rest.api.view.model.token

import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class TokenPriceInfoView (
    @Schema(title="Token Price in USD")
    val priceInUSD: Double,

    @Schema(title="Token Change Rate")
    val changeRate: Double,

    @Schema(title="Token 24h Volume")
    val volume24h: Double,

    @Schema(title="Token Circulating Market Cap")
    val circulatingMarketCap: Double,

    @Schema(title="Token On Chain Market Cap")
    val onChainMarketCap: Double,

    @Schema(title="Token Holders")
    val holders: Long,
)
