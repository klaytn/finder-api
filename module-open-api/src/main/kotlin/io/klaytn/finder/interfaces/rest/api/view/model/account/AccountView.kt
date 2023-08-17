package io.klaytn.finder.interfaces.rest.api.view.model.account

import io.klaytn.finder.domain.common.AccountType
import io.klaytn.finder.interfaces.rest.api.view.model.ContractSummary
import io.klaytn.finder.view.model.account.KlaytnAccountKeyView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema
data class AccountView(
    @Schema(title = "Address")
    val address: String,

    @Schema(title = "Account Type", example = "EOA, SCA")
    val accountType: AccountType,

    @Schema(title = "Balance")
    val balance: BigDecimal,

    @Schema(title = "Total Transaction Count")
    val totalTransactionCount: Long,

    @Schema(title = "Contract Information")
    val contract: ContractSummary?,

    @Schema(title = "Contract Creator Address")
    val contractCreatorAddress: String?,

    @Schema(title = "Contract Creator Transaction Hash")
    val contractCreatorTransactionHash: String?,

    @Schema(title = "Contract Created")
    val contractCreated: Boolean?,

    @Schema(title = "Account Key")
    val accountKey: KlaytnAccountKeyView?
)
