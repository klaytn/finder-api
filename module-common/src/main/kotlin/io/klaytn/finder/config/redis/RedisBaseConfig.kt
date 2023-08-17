package io.klaytn.finder.config.redis

import com.fasterxml.jackson.databind.ObjectMapper
import io.klaytn.finder.domain.redis.NftTokenUriContentRefreshRequest
import io.klaytn.finder.domain.redis.NftTokenUriRefreshRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisBaseConfig(
    private val objectMapper: ObjectMapper,
    private val redisConnectionFactory: RedisConnectionFactory,
) {
    @Bean
    fun redisTemplate() = RedisTemplate<String, String>().apply {
        setConnectionFactory(redisConnectionFactory)

        keySerializer = StringRedisSerializer()
        valueSerializer = StringRedisSerializer()
    }

    @Bean
    fun nftTokenUriRefreshRequestRedisTemplate() = RedisTemplate<String, NftTokenUriRefreshRequest>().apply {
        setConnectionFactory(redisConnectionFactory)

        keySerializer = StringRedisSerializer()
        valueSerializer = Jackson2JsonRedisSerializer(NftTokenUriRefreshRequest::class.java).apply {
            setObjectMapper(objectMapper)
        }
    }

    @Bean
    fun nftTokenUriContentRefreshRequestRedisTemplate() = RedisTemplate<String, NftTokenUriContentRefreshRequest>().apply {
        setConnectionFactory(redisConnectionFactory)

        keySerializer = StringRedisSerializer()
        valueSerializer = Jackson2JsonRedisSerializer(NftTokenUriContentRefreshRequest::class.java).apply {
            setObjectMapper(objectMapper)
        }
    }
}
