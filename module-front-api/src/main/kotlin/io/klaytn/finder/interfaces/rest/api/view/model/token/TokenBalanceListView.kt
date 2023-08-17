package io.klaytn.finder.interfaces.rest.api.view.model.token

import io.klaytn.finder.interfaces.rest.api.view.model.contract.ContractSummary
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.*

@Schema
data class TokenBalanceListView(
    @Schema(title="Contract Information")
    val info: ContractSummary,

    @Schema(title="Balance")
    val balance: BigDecimal,

    @Schema(title="Latest Transaction Timestamp")
    val latestTransactionDateTime: Date,
)
