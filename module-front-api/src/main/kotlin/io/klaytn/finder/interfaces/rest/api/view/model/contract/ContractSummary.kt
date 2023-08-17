package io.klaytn.finder.interfaces.rest.api.view.model.contract

import io.klaytn.finder.domain.mysql.set1.Contract
import io.klaytn.finder.infra.utils.applyDecimal
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema
data class ContractSummary(
    @Schema(title="Symbol")
    val symbol: String,

    @Schema(title="Name")
    val name: String,

    @Schema(title="Icon (BASE64)")
    val icon: String?,

    @Schema(title="Contract Address")
    val contractAddress: String,

    @Schema(title="Decimal")
    val decimal: Int,

    @Schema(title = "Verified")
    val verified: Boolean,

    @Schema(title="Total Supply")
    val totalSupply: BigDecimal,

    @Schema(title="Implementation Address")
    val implementationAddress: String?,
) {
    companion object {
        fun of(contract: Contract?) =
            contract?.let {
                ContractSummary(
                    name = it.name ?: it.contractAddress,
                    symbol = it.symbol ?: "-",
                    icon = it.icon,
                    contractAddress = it.contractAddress,
                    decimal = it.decimal,
                    verified = it.verified,
                    totalSupply = it.totalSupply.applyDecimal(it.decimal),
                    implementationAddress = it.implementationAddress
                )
            }

        fun of(contractAddress: String, contract: Contract?) =
            of(contract)
                ?: unknown(contractAddress)

        private fun unknown(contractAddress: String) =
            ContractSummary(
                name = contractAddress,
                symbol = "-",
                icon = null,
                contractAddress = contractAddress,
                decimal = 0,
                verified = false,
                totalSupply = BigDecimal.ZERO,
                implementationAddress = null
            )
    }
}
