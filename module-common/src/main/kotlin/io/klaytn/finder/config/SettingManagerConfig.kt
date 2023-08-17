package io.klaytn.finder.config

import io.klaytn.commons.curator.CuratorTemplate
import io.klaytn.commons.model.env.Phase
import io.klaytn.commons.setting.CuratorSettingManager
import io.klaytn.commons.setting.SettingManager
import io.klaytn.commons.setting.transmission.CuratorChangeTransmission
import io.klaytn.commons.setting.transmission.DynamicSettingTransmission
import org.apache.curator.framework.CuratorFramework
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.convert.DurationUnit
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import java.time.temporal.ChronoUnit

@Configuration
@EnableConfigurationProperties(SettingManagerProperty::class)
class SettingManagerConfig {
    @Bean
    fun curatorTemplate(curatorFramework: CuratorFramework,
                        settingManagerProperty: SettingManagerProperty,
                        phase: Phase
    ) = CuratorTemplate(curatorFramework, "${settingManagerProperty.rootPath}/$phase")

    @Bean
    fun dynamicSettingTransmission(applicationContext: ApplicationContext) =
        DynamicSettingTransmission(applicationContext)

    @Bean
    fun settingManager(
        curatorTemplate: CuratorTemplate,
        curatorChangeTransmissions: List<CuratorChangeTransmission>,
        applicationContext: ApplicationContext
    ): SettingManager =
        CuratorSettingManager(curatorTemplate, curatorChangeTransmissions, applicationContext)
}

@ConstructorBinding
@ConfigurationProperties(prefix = "finder.setting-manager")
data class SettingManagerProperty(
    val rootPath: String,
)