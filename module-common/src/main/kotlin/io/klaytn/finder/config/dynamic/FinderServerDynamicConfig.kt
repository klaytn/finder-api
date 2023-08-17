package io.klaytn.finder.config.dynamic

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.stereotype.Component

@Component
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy::class)
class FinderServerDynamicConfig(private val finderServerPaging: FinderServerPaging) {
    val paging = finderServerPaging
}

@Component
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy::class)
data class FinderServerPaging(
        val limit: FinderServerPagingLimitConfig,
        val interval: FinderServerPagingIntervalConfig
)
