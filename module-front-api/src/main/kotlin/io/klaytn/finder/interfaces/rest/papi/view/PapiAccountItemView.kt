package io.klaytn.finder.interfaces.rest.papi.view

import io.klaytn.finder.domain.common.AccountType
import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.interfaces.rest.api.view.model.contract.ContractSummary
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema
data class PapiAccountItemView(
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

    @Schema(title="Contract Creator Transaction Hash")
    val contractCreatorTransactionHash: String?,

    @Schema(title="Address Label")
    val addressLabel: String?,

    @Schema(title="KNS Domain")
    val knsDomain: String?,

    @Schema(title="Tag List")
    val tags: List<String>?
)
