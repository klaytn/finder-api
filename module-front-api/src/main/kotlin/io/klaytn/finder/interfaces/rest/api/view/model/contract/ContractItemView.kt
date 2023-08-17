package io.klaytn.finder.interfaces.rest.api.view.model.contract

import io.klaytn.finder.domain.common.ContractType
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema
data class ContractItemView(
    @Schema(title="Contract Address")
    val address: String,

    @Schema(title="Contract Type")
    val type: ContractType,

    @Schema(title="Contract Name")
    val name: String?,

    @Schema(title="Contract Symbol")
    val symbol: String?,

    @Schema(title="Contract Icon")
    var icon: String?,

    @Schema(title="Contract Official Site")
    val officialSite: String?,

    @Schema(title="Contract Official Email Address")
    val officialEmailAddress: String?,

    @Schema(title="Contract Decimal")
    val decimal: Int,

    @Schema(title="Contract Total Supply")
    var totalSupply: BigDecimal,

    @Schema(title="Contract Total Transfer")
    val totalTransfer: Long,

    @Schema(title="Contract Created")
    val contractCreated: Boolean,

    @Schema(title="Contract Implementation Address")
    val implementationAddress: String?,
)
