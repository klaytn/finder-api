package io.klaytn.finder.interfaces.rest.api.view.model.transaction

import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class TransactionInputDataView(
        @Schema(title = "Original Value") val originalValue: String,
        @Schema(title = "Decoded Value") val decodedValue: DecodedValue?,
        @Schema(title = "UTF-8 Value") val utf8Value: String,
)

@Schema
data class DecodedValue(
        @Schema(title = "Function Name") val signature: String,
        @Schema(title = "Method ID") val methodId: String,
        @Schema(title = "Parameter List") val parameters: List<DecodedParam>,
)

@Schema
data class DecodedParam(
        @Schema(title = "Parameter Type") val type: String,
        @Schema(title = "Parameter Name (if ABI exists)") val name: String?,
        @Schema(title = "Parameter Value") val value: String,
)
