package io.klaytn.finder.interfaces.rest.api.view.model.account

import io.klaytn.finder.interfaces.rest.api.view.model.ContractSummary
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigInteger
import java.util.*

@Schema
data class AccountNftBalanceListView(
    @Schema(title="Contract Information")
    val contract: ContractSummary,

    @Schema(title="Token ID")
    val tokenId: String? = null,

    @Schema(title="Number of NFTs")
    val tokenCount: BigInteger,

    @Schema(title="Last Transaction Timestamp")
    val latestTransaction: Date,
)