package io.klaytn.finder.service.redis

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import org.springframework.data.redis.connection.RedisClusterConfiguration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

class TestRedisConstants {
    companion object {
        private const val redisAddress = "REDIS_ENDPOINT"

        private val objectMapper =
                jacksonMapperBuilder()
                        .addModule(JavaTimeModule())
                        .propertyNamingStrategy(PropertyNamingStrategies.SnakeCaseStrategy())
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .build()
                        .apply {
                            this.activateDefaultTyping(
                                    this.polymorphicTypeValidator,
                                    ObjectMapper.DefaultTyping.EVERYTHING,
                                    JsonTypeInfo.As.PROPERTY
                            )
                        }

        fun getRedisConnectionFactory(): LettuceConnectionFactory {
            val redisClusterConfiguration = RedisClusterConfiguration(listOf(redisAddress))
            return LettuceConnectionFactory(redisClusterConfiguration).also {
                it.afterPropertiesSet()
            }
        }

        fun getStringRedisTemplate(
                redisConnectionFactory: RedisConnectionFactory
        ): RedisTemplate<String, String> {
            return RedisTemplate<String, String>()
                    .apply {
                        setConnectionFactory(redisConnectionFactory)
                        keySerializer = StringRedisSerializer()
                        valueSerializer = StringRedisSerializer()
                    }
                    .also { it.afterPropertiesSet() }
        }

        fun <T> getGenericRedisTemplate(
                redisConnectionFactory: RedisConnectionFactory,
                clazz: Class<T>
        ): RedisTemplate<String, T> {
            return RedisTemplate<String, T>()
                    .apply {
                        setConnectionFactory(redisConnectionFactory)

                        keySerializer = StringRedisSerializer()
                        valueSerializer =
                                Jackson2JsonRedisSerializer(clazz).apply {
                                    setObjectMapper(objectMapper)
                                }
                    }
                    .also { it.afterPropertiesSet() }
        }
    }
}
