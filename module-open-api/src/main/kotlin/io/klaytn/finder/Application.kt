package io.klaytn.finder

import io.klaytn.commons.utils.logback.handleSLF4JBridge
import io.klaytn.commons.utils.logback.logger
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(
    exclude = [
        JpaRepositoriesAutoConfiguration::class,
        DataSourceAutoConfiguration::class,
        ErrorMvcAutoConfiguration::class
    ]
)
@ConfigurationPropertiesScan
class Application

fun main(args: Array<String>) {
    handleSLF4JBridge()
    logger("Main").info("Active profile : {}", System.getProperty("spring.profiles.active"))

    runApplication<Application>(*args)
}
