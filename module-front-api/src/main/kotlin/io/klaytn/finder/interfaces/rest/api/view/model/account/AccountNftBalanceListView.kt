package io.klaytn.finder.interfaces.rest.api.view.model.account

import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.interfaces.rest.api.view.model.contract.ContractSummary
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigInteger
import java.util.*

@Schema
data class AccountNftBalanceListView(
    @Schema(title = "NFT Type", example = "KIP17, KIP37")
    val contractType: ContractType,

    @Schema(title="NFT Information")
    val info: ContractSummary,

    @Schema(title="Token ID")
    val tokenId: String? = null,

    @Schema(title="NFT Count")
    val tokenCount: BigInteger,

    @Schema(title="Last Transaction Timestamp")
    val latestTransaction: Date,
)
