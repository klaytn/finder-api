package io.klaytn.finder.interfaces.rest.api.view.model.nft

import io.klaytn.finder.view.model.account.AccountAddressView
import io.klaytn.finder.interfaces.rest.api.view.model.contract.ContractSummary
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigInteger
import java.util.*

@Schema
data class NftTransferListView(
    @Schema(title="Block #")
    val blockId: Long,

    @Schema(title="Transaction Hash")
    val transactionHash: String,

    @Schema(title="Transaction Timestamp")
    val datetime: Date,

    @Schema(title="Address (from)")
    val from: AccountAddressView,

    @Schema(title="Address (to)")
    val to: AccountAddressView,

    @Schema(title="NFT Contract Information")
    val nft: ContractSummary,

    @Schema(title="NFT Token ID")
    val tokenId: String,

    @Schema(title="NFT Token Count")
    val tokenCount: BigInteger,
)
