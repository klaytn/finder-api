package io.klaytn.finder.interfaces.rest.api.view.model

import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.domain.mysql.set1.Contract
import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class ContractSummary(
    @Schema(title = "Contract Address")
    val contractAddress: String,

    @Schema(title = "Contract Type")
    val contractType: ContractType?,
) {
    companion object {
        fun of(contract: Contract?) =
            contract?.let {
                ContractSummary(
                    contractAddress = it.contractAddress,
                    contractType = it.contractType
                )
            }

        fun of(contractAddress: String, contract: Contract?) =
            of(contract)
                ?: unknown(contractAddress)

        private fun unknown(contractAddress: String) =
            ContractSummary(
                contractAddress = contractAddress,
                contractType = null
            )
    }
}
