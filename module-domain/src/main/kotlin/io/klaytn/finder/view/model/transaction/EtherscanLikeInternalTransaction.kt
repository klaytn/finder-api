package io.klaytn.finder.view.model.transaction

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class EtherscanLikeInternalTransaction(
        @JsonProperty("hash") @Schema(title = "Transaction Hash") val hash: String,
        @JsonProperty("blockNumber") @Schema(title = "Block #") val blockNumber: String,
        @JsonProperty("timeStamp") @Schema(title = "Transaction Timestamp (timestamp)") val timeStamp: String,
        @JsonProperty("from") @Schema(title = "Address (from)") val from: String,
        @JsonProperty("to") @Schema(title = "Address (to)") val to: String,
        @JsonProperty("value") @Schema(title = "Amount Transferred") val value: String,
        @JsonProperty("gas") @Schema(title = "Gas Amount") val gas: String,
        @JsonProperty("isError") @Schema(title = "Transaction Error Status") val iserror: String,
        @JsonProperty("errCode") @Schema(title = "Transaction Error Code") val errCode: String,
        @JsonProperty("input") @Schema(title = "Transaction Data") val input: String,
        @JsonProperty("contractAddress") @Schema(title = "Contract Address") val contractAddress: String,
        @JsonProperty("gasUsed") @Schema(title = "Gas Used") val gasUsed: String,
        @JsonProperty("type") @Schema(title = "Transaction Type") val type: String,
        @JsonProperty("traceId") @Schema(title = "Transaction Trace ID") val traceId: String,
)
