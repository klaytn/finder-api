package io.klaytn.finder.infra.web.model

import io.swagger.v3.oas.annotations.Parameter
import javax.validation.constraints.Min
import org.springdoc.api.annotations.ParameterObject

@ParameterObject
data class BlockRangeRequest(
        @field:Parameter(
                name = "blockNumberStart",
                description = "start-number of block-range",
        )
        @field:Min(0)
        val blockNumberStart: Long?,
        @field:Parameter(
                name = "blockNumberEnd",
                description = "end-number of block-range",
        )
        @field:Min(0)
        val blockNumberEnd: Long?
) {
    fun toLongRange(): LongRange? =
            if (blockNumberStart != null && blockNumberEnd != null) {
                LongRange(blockNumberStart, blockNumberEnd)
            } else {
                null
            }
}
