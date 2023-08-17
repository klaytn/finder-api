package io.klaytn.finder.config

import io.klaytn.finder.config.dynamic.FinderMaintenanceDynamicConfig
import io.klaytn.finder.infra.web.interceptor.MaintenanceInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MaintenanceConfig {
    @Bean
    fun maintenanceInterceptor(finderMaintenanceDynamicConfig: FinderMaintenanceDynamicConfig) =
        MaintenanceInterceptor(finderMaintenanceDynamicConfig)
}
