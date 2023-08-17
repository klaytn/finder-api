package io.klaytn.finder.interfaces.rest.api.view.model.account

import io.klaytn.finder.domain.common.AccountType
import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.interfaces.rest.api.view.model.contract.ContractSummary
import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class AccountListView(
    @Schema(title="Address")
    val address: String,

    @Schema(title="Account Type", example = "EOA, SCA")
    val accountType: AccountType,

    @Schema(title="Contract Type", example = "Token, NFT17, NFT37")
    val contractType: ContractType?,

    @Schema(title="Contract Information")
    val info: ContractSummary?,

    @Schema(title="Klaytn Name Service Domain")
    val knsDomain: String?,

    @Schema(title="Address Label")
    val addressLabel: String?,

    @Schema(title="Tag List")
    val tags: List<String>?
)
