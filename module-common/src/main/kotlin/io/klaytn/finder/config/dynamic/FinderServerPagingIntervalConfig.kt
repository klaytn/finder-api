package io.klaytn.finder.config.dynamic

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.klaytn.commons.setting.dynamic.DynamicSetting
import io.klaytn.commons.setting.dynamic.DynamicSettingSupport
import io.klaytn.finder.config.ApplicationProperty
import org.springframework.stereotype.Component

@Component
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy::class)
class FinderServerPagingIntervalConfig(
    private val applicationProperty: ApplicationProperty,
): DynamicSettingSupport {
    override fun getRootPath() = applicationProperty.getApplicationDynamicConfigPath("server/paging/interval")

    @DynamicSetting val block: Long = 40_000L
    @DynamicSetting val transaction: Long = 1_000L
    @DynamicSetting val internalTransaction: Long = 1_000L
    @DynamicSetting val nftTransfer: Long = 1_000L
    @DynamicSetting val tokenTransfer: Long = 1_000L
    @DynamicSetting val tokenBurn: Long = 1_000L
    @DynamicSetting val eventLog: Long = 1_000L
}