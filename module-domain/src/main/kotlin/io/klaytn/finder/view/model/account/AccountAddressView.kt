package io.klaytn.finder.view.model.account

import io.klaytn.finder.domain.common.AccountType
import io.klaytn.finder.domain.common.ContractType
import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class AccountAddressView(
    @Schema(title="Address")
    val address: String,

    @Schema(title="Account Type", example = "EOA, SCA")
    val accountType: AccountType?,

    @Schema(title="Contract Type", example = "Token, NFT17, NFT37")
    val contractType: ContractType?,

    @Schema(title="Token/NFT Symbol")
    val symbol: String?,

    @Schema(title="Token/NFT Name")
    val name: String?,

    @Schema(title="Token/NFT Icon")
    val icon: String?,

    @Schema(title = "Token/NFT Verified Status")
    val verified: Boolean?,

    @Schema(title="Klaytn Name Service Domain")
    val knsDomain: String?,

    @Schema(title="Address Label")
    val addressLabel: String?,
) {
    companion object {
        fun of(address: String) =
            AccountAddressView(address, null, null, null, null, null, null, null, null)
    }
}
