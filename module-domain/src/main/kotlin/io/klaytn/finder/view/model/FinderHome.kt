package io.klaytn.finder.view.model

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Schema
data class FinderStatus(
        @Schema(title = "Block #") val blockHeight: Long,
        @Schema(title = "Block Creation Time") val datetime: LocalDateTime,
        @Schema(title = "Burnt Information up to Current Block") val blockBurnt: BlockBurnView?,
)

data class FinderSummary(
        @Schema(title = "Number of Consensus Nodes") val consensusNode: Int,
        @Schema(title = "Average Block Creation Time") val averageBlockTime: String,
        @Schema(title = "Average Transactions per Block") val averageTxPerBlock: Int,
        @Schema(title = "Transactions per Second") val transactionPerSec: Long,
)

data class FinderKlayPrice(
        @Schema(title = "Klay Dollar Price") val usdPrice: BigDecimal,
        @Schema(title = "Klay Bitcoin Price") val btcPrice: BigDecimal,
        @Schema(title = "Klay Dollar Price Change Ratio") val usdPriceChanges: BigDecimal,
        @Schema(title = "Market Cap") val marketCap: BigDecimal,
        @Schema(title = "Total Supply") val totalSupply: BigDecimal,
        @Schema(title = "Klay Dollar Volume 24h") val volume: BigDecimal,
)

@Schema
data class FinderTransactionHistory(
        @Schema(title = "Total Transactions") val total: BigDecimal,
        @Schema(title = "Transaction History") val histories: List<FinderTransactionCount>,
)

@Schema
data class FinderTransactionCount(
        @Schema(title = "Date") val date: LocalDate,
        @Schema(title = "Transaction Count") val count: Long,
)

@Schema
data class FinderBurntByGasFeeHistory(
        @Schema(title = "Burnt by Gas Fee History") val histories: List<FinderBurntByGasFee>,
)

@Schema
data class FinderBurntByGasFee(
        @Schema(title = "Date") val date: LocalDate,
        @Schema(title = "Burnt Amount") val amount: BigDecimal,
)
