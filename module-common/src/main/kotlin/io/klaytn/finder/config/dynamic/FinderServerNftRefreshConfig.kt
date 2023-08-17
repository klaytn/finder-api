package io.klaytn.finder.config.dynamic

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.klaytn.commons.setting.dynamic.DynamicSetting
import io.klaytn.commons.setting.dynamic.DynamicSettingSupport
import io.klaytn.finder.config.ApplicationProperty
import org.springframework.stereotype.Component

@Component
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy::class)
class FinderServerNftRefreshConfig(
    private val applicationProperty: ApplicationProperty,
): DynamicSettingSupport {
    override fun getRootPath() = applicationProperty.getApplicationDynamicConfigPath("server/nft/refresh")

    /**
     * seconds
     */
    @DynamicSetting val tokenUriRefreshLockTime: Long = 60
}