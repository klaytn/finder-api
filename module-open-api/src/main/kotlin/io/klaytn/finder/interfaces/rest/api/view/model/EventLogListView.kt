package io.klaytn.finder.interfaces.rest.api.view.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class EventLogListView(
    @Schema(title = "Log Index") val logIndex: Int,
    @Schema(title = "Contract Address") val contractAddress: String,
    @Schema(title = "Type") val type: String?,
    @Schema(title = "Topics") val topics: List<String>,
    @Schema(title = "Data") val data: String,
    @Schema(title = "Item List") val items: List<EventLogItem>,
    @Schema(title = "Block #") val blockNumber: Long,
    @Schema(title = "Transaction Hash") val transactionHash: String?,
    @Schema(title = "Estimated Event") val estimatedEventLog: Boolean?
)

@Schema
data class EventLogItem(
    @Schema(title = "Name") val name: String,
    @Schema(title = "Value") val value: String,
)
