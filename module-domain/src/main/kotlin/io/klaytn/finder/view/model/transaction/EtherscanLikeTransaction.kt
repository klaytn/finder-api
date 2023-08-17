package io.klaytn.finder.view.model.transaction

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class EtherscanLikeTransaction(
        @JsonProperty("hash") @Schema(title = "Transaction Hash") val hash: String,
        @JsonProperty("blockNumber") @Schema(title = "Block #") val blockNumber: String,
        @JsonProperty("timeStamp") @Schema(title = "Transaction Timestamp (timestamp)") val timeStamp: String,
        @JsonProperty("nonce") @Schema(title = "Nonce") val nonce: String,
        @JsonProperty("blockHash") @Schema(title = "Block Hash") val blockHash: String,
        @JsonProperty("transactionIndex") @Schema(title = "Transaction Index") val transactionIndex: String,
        @JsonProperty("from") @Schema(title = "Address (from)") val from: String,
        @JsonProperty("to") @Schema(title = "Address (to)") val to: String,
        @JsonProperty("value") @Schema(title = "Amount Transferred") val value: String,
        @JsonProperty("gas") @Schema(title = "Gas Amount") val gas: String,
        @JsonProperty("gasPrice") @Schema(title = "Gas Price") val gasPrice: String,
        @JsonProperty("isError") @Schema(title = "Transaction Error Status") val iserror: String,
        @JsonProperty("txreceipt_status")
        @Schema(title = "Transaction Status (0: Failed, 1: Successful)")
        val txreceipt_status: String,
        @JsonProperty("input") @Schema(title = "Transaction Data") val input: String,
        @JsonProperty("contractAddress") @Schema(title = "Contract Address") val contractAddress: String,
        @JsonProperty("gasUsed") @Schema(title = "Gas Used") val gasUsed: String,
        @JsonProperty("confirmations") @Schema(title = "Block Confirmations") val confirmations: String,
        @JsonProperty("methodId") @Schema(title = "Method ID") val methodId: String,
        @JsonProperty("functionName") @Schema(title = "Function Name") val functionName: String,
        @JsonProperty("cumulativeGasUsed")
        @Schema(title = "Cumulative Gas Used")
        val cumulativeGasUsed: String,
)
