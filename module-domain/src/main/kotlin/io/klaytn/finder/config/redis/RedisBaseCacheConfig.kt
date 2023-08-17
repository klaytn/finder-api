package io.klaytn.finder.config.redis

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import io.klaytn.finder.infra.cache.CacheName
import io.klaytn.finder.infra.redis.RedisKeyManager
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
import java.time.Duration
import java.time.Duration.*

@EnableCaching
@Configuration
class RedisBaseCacheConfig(
    private val redisKeyManager: RedisKeyManager
) {
    @Bean
    fun genericJackson2JsonRedisSerializer(): GenericJackson2JsonRedisSerializer {
        val objectMapper = jacksonMapperBuilder()
            .addModule(JavaTimeModule())
            .propertyNamingStrategy(PropertyNamingStrategies.SnakeCaseStrategy())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build().apply {
                this.activateDefaultTyping(
                    this.polymorphicTypeValidator, ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY
                )
            }

        return GenericJackson2JsonRedisSerializer(objectMapper)
    }

    /**
     * default redis cache configuration
     */
    @Bean
    fun cacheConfiguration(genericJackson2JsonRedisSerializer: GenericJackson2JsonRedisSerializer) =
        getRedisCacheConfiguration(genericJackson2JsonRedisSerializer, ofMinutes(10))

    /**
     * custom redis cache configuration
     */
    @Bean
    fun redisCacheManagerBuilderCustomizer(genericJackson2JsonRedisSerializer: GenericJackson2JsonRedisSerializer) =
        RedisCacheManagerBuilderCustomizer { builder: RedisCacheManagerBuilder ->
            builder
                .withInitialCacheConfigurations(
                    mapOf(
                        CacheName.ACCOUNT_BY_ADDRESS to
                                getRedisCacheConfiguration(genericJackson2JsonRedisSerializer, ofSeconds(30)),
                        CacheName.TOKEN_HOLDER to
                                getRedisCacheConfiguration(genericJackson2JsonRedisSerializer, ofSeconds(10)),
                        CacheName.TOKEN_HOLDER_COUNT_BY_CONTRACT to
                                getRedisCacheConfiguration(genericJackson2JsonRedisSerializer, ofSeconds(10)),
                        CacheName.NFT_17_HOLDER to
                                getRedisCacheConfiguration(genericJackson2JsonRedisSerializer, ofSeconds(10)),
                        CacheName.NFT_17_HOLDER_COUNT_BY_CONTRACT to
                                getRedisCacheConfiguration(genericJackson2JsonRedisSerializer, ofSeconds(10)),
                        CacheName.NFT_INVENTORY to
                                getRedisCacheConfiguration(genericJackson2JsonRedisSerializer, ofSeconds(10)),
                        CacheName.NFT_INVENTORY_COUNT_BY_CONTRACT to
                                getRedisCacheConfiguration(genericJackson2JsonRedisSerializer, ofSeconds(10)),
                        CacheName.BLOCK_LATEST_NUMBER to
                                getRedisCacheConfiguration(genericJackson2JsonRedisSerializer, ofSeconds(1)),
                        CacheName.TRANSACTION_LATEST_ID to
                                getRedisCacheConfiguration(genericJackson2JsonRedisSerializer, ofSeconds(10)),
                        CacheName.CAVER_NFT_TOKEN_ITEM_TOTAL_SUPPLY to
                                getRedisCacheConfiguration(genericJackson2JsonRedisSerializer, ofSeconds(5)),
                        CacheName.ACCOUNT_ADDRESS_BY_KNS to
                                getRedisCacheConfiguration(genericJackson2JsonRedisSerializer, ofSeconds(10)),
                        CacheName.GAS_PRICE to
                                getRedisCacheConfiguration(genericJackson2JsonRedisSerializer, ofDays(1)),
                        CacheName.ACCOUNT_RELATED_INFOS to
                                getRedisCacheConfiguration(genericJackson2JsonRedisSerializer, ofSeconds(5)),
                        CacheName.FUNCTION_SIGNATURE to
                                getRedisCacheConfiguration(genericJackson2JsonRedisSerializer, ofDays(1)),
                        CacheName.EVENT_SIGNATURE to
                                getRedisCacheConfiguration(genericJackson2JsonRedisSerializer, ofDays(1)),
                        CacheName.BLOCK_BURN to
                                getRedisCacheConfiguration(genericJackson2JsonRedisSerializer, ofSeconds(3)),
                        CacheName.BLOCK_REWARD_BY_NUMBER to
                                getRedisCacheConfiguration(genericJackson2JsonRedisSerializer, ofSeconds(30)),
                        CacheName.STAT_TOTAL_TRANSACTION_COUNT to
                                getRedisCacheConfiguration(genericJackson2JsonRedisSerializer, ofSeconds(1)),
                    )
                )
        }

    private fun getRedisCacheConfiguration(
        genericJackson2JsonRedisSerializer: GenericJackson2JsonRedisSerializer,
        ttl: Duration,
    ) =
        RedisCacheConfiguration.defaultCacheConfig()
            .prefixCacheNameWith("${redisKeyManager.chainCachePrefix}/")
            .entryTtl(ttl)
            .serializeValuesWith(SerializationPair.fromSerializer(genericJackson2JsonRedisSerializer))
}
