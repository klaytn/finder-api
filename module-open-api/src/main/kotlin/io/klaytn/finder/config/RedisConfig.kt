package io.klaytn.finder.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory

@Configuration
class RedisConfig(
        private val objectMapper: ObjectMapper,
        private val redisConnectionFactory: RedisConnectionFactory,
) {}
