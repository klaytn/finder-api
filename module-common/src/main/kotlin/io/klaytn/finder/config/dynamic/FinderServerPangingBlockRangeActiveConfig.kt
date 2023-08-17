package io.klaytn.finder.config.dynamic

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.klaytn.commons.setting.dynamic.DynamicSetting
import io.klaytn.commons.setting.dynamic.DynamicSettingSupport
import io.klaytn.finder.config.ApplicationProperty
import org.springframework.stereotype.Component

@Component
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy::class)
class FinderServerPangingBlockRangeActiveConfig(
    private val applicationProperty: ApplicationProperty,
): DynamicSettingSupport {
    override fun getRootPath() = applicationProperty.getApplicationDynamicConfigPath("server/paging/blockRangeActive")

    @DynamicSetting val block: Boolean = false
    @DynamicSetting val transaction: Boolean = false
    @DynamicSetting val internalTransaction: Boolean = false
    @DynamicSetting val nftTransfer: Boolean = false
    @DynamicSetting val tokenTransfer: Boolean = false
    @DynamicSetting val tokenBurn: Boolean = false
    @DynamicSetting val eventLog: Boolean = false
}