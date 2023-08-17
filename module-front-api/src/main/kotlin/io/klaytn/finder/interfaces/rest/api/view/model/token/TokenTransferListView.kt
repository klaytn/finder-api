package io.klaytn.finder.interfaces.rest.api.view.model.token

import io.klaytn.finder.view.model.account.AccountAddressView
import io.klaytn.finder.interfaces.rest.api.view.model.contract.ContractSummary
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.Date

@Schema
data class TokenTransferListView(
    @Schema(title="Block #")
    val blockId: Long,

    @Schema(title="Transaction Hash")
    val transactionHash: String,

    @Schema(title="Transaction Time")
    val datetime: Date,

    @Schema(title="Address (from)")
    val from: AccountAddressView,

    @Schema(title="Address (to)")
    val to: AccountAddressView,

    @Schema(title="Token Contract Information")
    val token: ContractSummary,

    @Schema(title="Token Transfer Amount")
    val amount: BigDecimal,
)
