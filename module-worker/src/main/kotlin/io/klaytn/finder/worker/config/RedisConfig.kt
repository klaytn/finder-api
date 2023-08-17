package io.klaytn.finder.worker.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.klaytn.finder.worker.infra.redis.RedisKeyManagerForWorker
import io.klaytn.finder.worker.infra.redis.RedisLockUtilsForWorker
import io.klaytn.finder.worker.interfaces.rabbitmq.solidity.SolidityCompilerUploadRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig(
    private val objectMapper: ObjectMapper,
    private val redisConnectionFactory: RedisConnectionFactory
) {
    @Bean
    fun solidityCompilerUploadRedisTemplate() = RedisTemplate<String, SolidityCompilerUploadRequest>().apply {
        setConnectionFactory(redisConnectionFactory)

        keySerializer = StringRedisSerializer()
        valueSerializer = Jackson2JsonRedisSerializer(SolidityCompilerUploadRequest::class.java).apply {
            setObjectMapper(objectMapper)
        }
    }

    @Bean
    fun redisLockUtils(
        redisTemplate: RedisTemplate<String, String>,
        redisKeyManagerForWorker: RedisKeyManagerForWorker
    ) = RedisLockUtilsForWorker(redisTemplate, redisKeyManagerForWorker)
}
