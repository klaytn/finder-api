package io.klaytn.finder.view.model.transaction

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class EtherscanLikeTokenTransaction(
        @JsonProperty("hash") @Schema(title = "Transaction Hash") val hash: String,
        @JsonProperty("blockNumber") @Schema(title = "Block #") val blockNumber: String,
        @JsonProperty("timeStamp") @Schema(title = "Transaction Timestamp (timestamp)") val timeStamp: String,
        @JsonProperty("nonce") @Schema(title = "Nonce") var nonce: String,
        @JsonProperty("blockHash") @Schema(title = "Block Hash") var blockHash: String,
        @JsonProperty("transactionIndex") @Schema(title = "Transaction Index") var transactionIndex: String,
        @JsonProperty("from") @Schema(title = "Address (from)") val from: String,
        @JsonProperty("to") @Schema(title = "Address (to)") val to: String,
        @JsonProperty("value") @Schema(title = "Amount Transferred") val value: String,
        @JsonProperty("gas") @Schema(title = "Gas Amount") var gas: String,
        @JsonProperty("gasPrice") @Schema(title = "Gas Price") var gasPrice: String,
        @JsonProperty("input") @Schema(title = "Transaction Data") var input: String,
        @JsonProperty("contractAddress") @Schema(title = "Contract Address") val contractAddress: String,
        @JsonProperty("gasUsed") @Schema(title = "Gas Used") var gasUsed: String,
        @JsonProperty("confirmations") @Schema(title = "Block Confirmations") var confirmations: String,
        @JsonProperty("tokenName") @Schema(title = "Token Name") var tokenName: String,
        @JsonProperty("tokenSymbol") @Schema(title = "Token Symbol") var tokenSymbol: String,
        @JsonProperty("tokenDecimal") @Schema(title = "Token Decimal") var tokenDecimal: String,
        @JsonProperty("cumulativeGasUsed") @Schema(title = "Cumulative Gas Used") var cumulativeGasUsed: String
)
