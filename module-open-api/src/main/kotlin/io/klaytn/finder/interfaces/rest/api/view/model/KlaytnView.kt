package io.klaytn.finder.interfaces.rest.api.view.model

import io.klaytn.finder.view.model.FinderKlayPrice
import io.klaytn.finder.view.model.FinderSummary
import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class KlaytnView(
    @Schema(title = "klaytn summary")
    val summary: FinderSummary,

    @Schema(title = "klay price")
    val klayPrice: FinderKlayPrice,
)
