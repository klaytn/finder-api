package io.klaytn.finder.interfaces.rest.api.view.model.contract

import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class ContractCodeView(
        @Schema(title = "Contract Name") val contractName: String,
        @Schema(title = "Compiler Version") val compilerVersion: String,
        @Schema(title = "Contract Source Code") val contractSourceCode: String,
        @Schema(title = "Contract ABI") val contractABI: String,
        @Schema(title = "Contract Creation Code") val contractCreationCode: String,
        @Schema(title = "ABI Encoded Value") val abiEncodedValue: String?,
        @Schema(title = "Optimization Flag") val optimizationFlag: Boolean,
        @Schema(title = "Optimization Runs Count") val optimizationRunsCount: Long,
        @Schema(title = "EVM Version") val evmVersion: String?,
        @Schema(title = "License Type") val licenseType: String?,
        @Schema(title = "Proxy Contract Type") val proxyContractType: Boolean?,
        @Schema(title = "Implementation Contract Type") val implementationContractType: Boolean?,
        @Schema(title = "Implementation Contract Source Information")
        val implementationContractCode: RelatedContractCodeView?,
        @Schema(title = "Proxy Contract Source Information") val proxyContractCode: RelatedContractCodeView?,
)

@Schema
data class RelatedContractCodeView(
        @Schema(title = "Contract Address") val contractAddress: String,
        @Schema(title = "Contract Name") val contractName: String,
        @Schema(title = "Contract ABI") val contractABI: String,
)
