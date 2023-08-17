package io.klaytn.finder.interfaces.rest.api.view.model.nft

import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.view.model.account.AccountAddressView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigInteger
import java.util.*

@Schema
data class NftInventoryListView(
    @Schema(title = "Contract Type", example = "KIP17, KIP37")
    val contractType: ContractType,

    @Schema(title="Token ID")
    val tokenId: String,

    @Schema(title="Holder")
    val holder: AccountAddressView,

    @Schema(title="Token URI")
    val tokenUri: String,

    @Schema(title="Token Count")
    val tokenCount: BigInteger? = null,

    @Schema(title="Token Updated At (UTC)")
    val updatedAt: Date,
)
