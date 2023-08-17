package io.klaytn.finder.view.model.account

import io.klaytn.finder.service.accountkey.KlaytnAccountKeyType
import io.klaytn.finder.view.model.transaction.TransactionTypeView
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema
data class AccountKeyListView(
    @Schema(title="Block #")
    val blockNumber: Long,

    @Schema(title="Transaction Hash")
    val transactionHash: String,

    @Schema(title="Transaction Type")
    val transactionType: TransactionTypeView,

    @Schema(title="Account Address")
    val accountAddress: String,

    @Schema(title="Account Key Type")
    val accountKeyType: KlaytnAccountKeyType?,

    @Schema(title="Account Key")
    val key: String?,

    @Schema(title="Account Key")
    val accountKey: KlaytnAccountKeyView?,

    @Schema(title="Transaction Timestamp (UTC)")
    val datetime: Date,
)
