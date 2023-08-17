package io.klaytn.finder.config.dynamic

import io.klaytn.commons.setting.dynamic.DynamicSetting
import io.klaytn.commons.setting.dynamic.DynamicSettingSupport
import io.klaytn.finder.config.ApplicationProperty
import org.springframework.stereotype.Component

@Component
class FinderMaintenanceDynamicConfig(
    private val applicationProperty: ApplicationProperty
): DynamicSettingSupport {
    override fun getRootPath() = applicationProperty.getApplicationDynamicConfigPath("server/maintenance")

    @DynamicSetting var status: Boolean = false
    @DynamicSetting var reason: String? = null
}