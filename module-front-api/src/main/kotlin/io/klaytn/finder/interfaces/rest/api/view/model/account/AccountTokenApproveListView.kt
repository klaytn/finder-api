package io.klaytn.finder.interfaces.rest.api.view.model.account

import io.klaytn.finder.interfaces.rest.api.view.model.contract.ContractSummary
import io.klaytn.finder.view.model.account.AccountAddressView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.*

@Schema
data class AccountTokenApproveListView(
    @Schema(title="Block #")
    val blockNumber: Long,

    @Schema(title="Transaction Hash")
    val transactionHash: String,

    @Schema(title="Contract Information")
    val contractSummary: ContractSummary?,

    @Schema(title="Spender Account")
    val spenderAccount: AccountAddressView,

    @Schema(title="Approved Amount")
    val approvedAmount: BigDecimal,

    @Schema(title="Transaction Timestamp")
    val timestamp: Date,
)
