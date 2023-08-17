package io.klaytn.finder.config.dynamic

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.klaytn.commons.setting.dynamic.DynamicSetting
import io.klaytn.commons.setting.dynamic.DynamicSettingSupport
import io.klaytn.finder.config.ApplicationProperty
import org.springframework.stereotype.Component

@Component
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy::class)
class FinderServerFeatureConfig(
    private val applicationProperty: ApplicationProperty,
): DynamicSettingSupport {
    override fun getRootPath() = applicationProperty.getApplicationDynamicConfigPath("server/features")

    @DynamicSetting val governanceCouncil: Boolean = false
    @DynamicSetting val estimatedEventLog: Boolean = false
    @DynamicSetting val contractSubmissionConstructorArguments: Boolean = false
    @DynamicSetting val accountTransferContractWithJoin: Boolean = false
    @DynamicSetting val transactionPerSecCheckUnit: String = "sec"                  // sec, hour
}