package io.klaytn.finder.interfaces.rest.api.view.model.account

import io.klaytn.finder.domain.common.AccountType
import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.interfaces.rest.api.view.model.contract.ContractSummary
import io.klaytn.finder.interfaces.rest.api.view.model.governancecouncil.GovernanceCouncilView
import io.klaytn.finder.view.model.account.AccountAddressView
import io.klaytn.finder.view.model.account.KlaytnAccountKeyView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema
data class AccountItemView(
    @Schema(title="Address")
    val address: String,

    @Schema(title="Account Type", example = "EOA, SCA")
    val accountType: AccountType,

    @Schema(title="Balance")
    val balance: BigDecimal,

    @Schema(title="Total Transaction Count")
    val totalTransactionCount: Long,

    @Schema(title="Contract Type", example = "Token, NFT17, NFT37")
    val contractType: ContractType?,

    @Schema(title="Contract Information")
    val info: ContractSummary?,

    @Schema(title="Contract Creator Address")
    val contractCreatorAddress: String?,

    @Schema(title="Contract Creator")
    val contractCreator: AccountAddressView?,

    @Schema(title="Contract Creation Transaction Hash")
    val contractCreatorTransactionHash: String?,

    @Schema(title="Contract Created")
    val contractCreated: Boolean?,

    @Schema(title="Klaytn Name Service Domain")
    val knsDomain: String?,

    @Schema(title="Address Label")
    val addressLabel: String?,

    @Schema(title="Tag List")
    val tags: List<String>?,

    @Schema(title="Associated Information")
    val associatedInfos: Map<String, Boolean>?,

    @Schema(title="Governance Council Information")
    val governanceCouncil: GovernanceCouncilView?,

    @Schema(title="Account Key")
    val accountKey: KlaytnAccountKeyView?
)
