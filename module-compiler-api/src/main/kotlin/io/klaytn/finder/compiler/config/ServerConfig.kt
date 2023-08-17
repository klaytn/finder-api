package io.klaytn.finder.compiler.config

import io.klaytn.commons.model.env.Phase
import io.klaytn.commons.utils.logback.logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class ServerConfig {
    private val logger = logger(ServerConfig::class.java)

    @Bean
    fun phase(env: Environment) = Phase.fromProfiles(env.activeProfiles).also {
        logger.info("Activate profile: {} as phase: {}", env.activeProfiles, it)
    }
}
