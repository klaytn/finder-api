package io.klaytn.finder.infra.web.model

import io.swagger.v3.oas.annotations.Parameter
import javax.validation.constraints.Min
import org.springdoc.api.annotations.ParameterObject

@ParameterObject
data class EtherscanLikeBlockRangeRequest(
        @field:Parameter(
                name = "startblock",
                description = "the integer block number to start searching for transactions",
        )
        @field:Min(0)
        val startblock: Long?,
        @field:Parameter(
                name = "endblock",
                description = "the integer block number to stop searching for transactions",
        )
        @field:Min(0)
        val endblock: Long?,
) {
    fun toLongRange(): LongRange? =
            if (startblock != null && endblock != null) {
                LongRange(startblock, endblock)
            } else {
                null
            }
    fun blockNumberStart(): Long? = startblock
    fun blockNumberEnd(): Long? = endblock
}
