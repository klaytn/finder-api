package io.klaytn.finder.interfaces.rest.api.view.model.nft

import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.view.model.account.AccountAddressView
import io.klaytn.finder.interfaces.rest.api.view.model.contract.ContractSummary
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigInteger
import java.util.*

@Schema
data class NftTokenItemView(
    @Schema(title = "Contract Type", example = "KIP17, KIP37")
    val contractType: ContractType,

    @Schema(title="NFT Information")
    val info: ContractSummary,

    @Schema(title="NFT Token ID")
    val tokenId: String,

    @Schema(title="Token URI")
    val tokenUri: String,

    /**
     * Applicable to KIP17
     */
    @Schema(title="(only KIP17) Holder Address")
    val holder: AccountAddressView?,

    /**
     * Applicable to KIP37
     */
    @Schema(title="(only KIP37) Total Supply of Token ID")
    val totalSupply: BigInteger?,
    @Schema(title="(only KIP37) Total Transfers of Token ID")
    val totalTransfer: Long?,
    @Schema(title="(only KIP37) Burn Amount of Token ID")
    val burnAmount: BigInteger?,
    @Schema(title="(only KIP37) Total Burns of Token ID")
    val totalBurn: Long?,

    @Schema(title="Token URI Refreshable State")
    val tokenUriRefreshable: Boolean?,

    @Schema(title="Token URI Refresh Timestamp (UTC)")
    val tokenUriUpdatedAt: Date,
)
