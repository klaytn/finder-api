package io.klaytn.finder.interfaces.rest.api.view.model.nft

import io.klaytn.finder.interfaces.rest.api.view.model.ContractSummary
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigInteger
import java.util.*

@Schema
data class NftTransferListView(
    @Schema(title = "Contract Information")
    val contract: ContractSummary,

    @Schema(title = "Block #")
    val blockId: Long,

    @Schema(title = "Transaction Hash")
    val transactionHash: String,

    @Schema(title = "Fee Payer")
    val feePayer: String?,

    @Schema(title = "Transaction Index")
    val transactionIndex: Int?,

    @Schema(title = "Transaction Timestamp")
    val datetime: Date,

    @Schema(title = "Address (from)")
    val from: String,

    @Schema(title = "Address (to)")
    val to: String?,

    @Schema(title = "NFT Token ID")
    val tokenId: String,

    @Schema(title = "NFT Token Count")
    val tokenCount: BigInteger,
)
