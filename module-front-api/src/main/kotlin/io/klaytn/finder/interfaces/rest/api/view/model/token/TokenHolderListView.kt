package io.klaytn.finder.interfaces.rest.api.view.model.token

import io.klaytn.finder.view.model.account.AccountAddressView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema
data class TokenHolderListView(
    @Schema(title="Holder")
    val holder: AccountAddressView,

    @Schema(title="Amount")
    val amount: BigDecimal,

    @Schema(title="Percentage")
    val percentage: BigDecimal?,
)
