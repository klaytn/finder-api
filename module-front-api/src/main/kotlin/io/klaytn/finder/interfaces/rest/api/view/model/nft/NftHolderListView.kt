package io.klaytn.finder.interfaces.rest.api.view.model.nft

import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.view.model.account.AccountAddressView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.math.BigInteger

@Schema
data class NftHolderListView(
    @Schema(title = "Rank", example = "KIP17, KIP37")
    val contractType: ContractType,

    @Schema(title="Holder")
    val holder: AccountAddressView,

    @Schema(title="Quantity")
    val tokenCount: BigInteger,

    @Schema(title="Percentage")
    val percentage: BigDecimal? = null,

    @Schema(title="Token ID")
    val tokenId: String? = null,
)
