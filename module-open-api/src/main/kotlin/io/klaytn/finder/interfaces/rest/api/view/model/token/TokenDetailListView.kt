package io.klaytn.finder.interfaces.rest.api.view.model.token

import io.klaytn.finder.interfaces.rest.api.view.model.ContractDetail
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.*

@Schema
data class TokenDetailListView(
    @Schema(title = "Contract Information")
    val contract: ContractDetail,

    @Schema(title = "Balance")
    val balance: BigDecimal,

    @Schema(title = "Latest Transaction DateTime")
    val latestTransactionDateTime: Date,
)
