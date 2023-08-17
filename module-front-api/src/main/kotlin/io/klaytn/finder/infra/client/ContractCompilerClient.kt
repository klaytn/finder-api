package io.klaytn.finder.infra.client

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ContractCompilerClient {
    @GET("/options") fun getOptions(): Call<OptionResult>

    @POST("/compile")
    fun compileContractCode(@Body request: CompileRequest): Call<List<CompileResult>>
}

data class OptionResult(
        val licenses: List<String>,
        @JsonProperty("solidity_versions") val solidityVersions: List<String>,
        @JsonProperty("evm_versions") val evmVersions: List<EvmVersion>,
) {
    @Schema
    data class EvmVersion(
            @Schema val name: String,
            @Schema val desc: String,
    )
}

@Schema
data class CompileRequest(
        @Schema val version: String,
        @Schema val license: String,
        @Schema val optimize: Boolean,
        @Schema @JsonProperty("optimize_runs") val optimizeRuns: Long,
        @Schema @JsonProperty("evm_version") val evmVersion: String?,
        @Schema val libraries: Map<String, String>?,
        @Schema val solidity: String,
)

@Schema
data class CompileResult(
        @Schema val name: String,
        @Schema val abi: String,
        @Schema val binary: String,
        @Schema @JsonProperty("runtime_binary") val runtimeBinary: String,
        @Schema val hashes: Map<String, String>,
        @Schema @JsonProperty("abi_functions") val abiFunctions: List<ABIFunction>,
        @Schema @JsonProperty("abi_events") val abiEvents: List<ABIEvent>,
)

@Schema
data class ABIFunction(
        @Schema val constant: Boolean,
        @Schema val inputs: List<ABIFunctionParam>,
        @Schema val name: String,
        @Schema val outputs: List<ABIFunctionParam>,
        @Schema val payable: Boolean,
        @Schema @JsonProperty("state_mutability") val stateMutability: String,
)

@Schema
data class ABIEvent(
        @Schema val anonymous: Boolean,
        @Schema val inputs: List<ABIEventParam>,
        @Schema val name: String,
)

@Schema
data class ABIFunctionParam(
        @Schema val name: String,
        @Schema val type: String,
)

@Schema
data class ABIEventParam(
        @Schema val indexed: Boolean,
        @Schema val name: String,
        @Schema val type: String,
)
