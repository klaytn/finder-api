package io.klaytn.finder.worker

import io.klaytn.commons.utils.logback.handleSLF4JBridge
import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.config.*
import io.klaytn.finder.config.redis.RedisBaseConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication(
    exclude = [
        JpaRepositoriesAutoConfiguration::class,
        DataSourceAutoConfiguration::class,
        ErrorMvcAutoConfiguration::class
    ]
)
@ConfigurationPropertiesScan
@Import(
    value = [
        ApplicationConfig::class,
        MessageConfig::class,
        GcpStorageConfig::class,
        RedisBaseConfig::class,
        JobConfig::class,
        ServerConfig::class,
        SettingManagerConfig::class,
    ]
)
class Application

fun main(args: Array<String>) {
    handleSLF4JBridge()
    logger("Main").info("Active profile : {}", System.getProperty("spring.profiles.active"))

    runApplication<Application>(*args)
}
