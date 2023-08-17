package io.klaytn.finder.view.model.transaction

import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class TransactionStatusView(
        @Schema(title = "Transaction Status") val status: TransactionStatus,
        @Schema(title = "Message when Transaction Status is Fail") val failMessage: String?,
)
