package io.klaytn.finder.infra.cache

import io.klaytn.finder.domain.mysql.BaseEntity
import io.klaytn.finder.domain.mysql.BaseRepository
import io.klaytn.finder.infra.redis.RedisKeyManager
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.stereotype.Service

@Service
class CacheUtils(
    private val genericJackson2JsonRedisSerializer: GenericJackson2JsonRedisSerializer,
    private val redisTemplate: RedisTemplate<String, String>,
    private val cacheManager: RedisCacheManager,
    private val redisKeyManager: RedisKeyManager
) {
    fun <T : BaseEntity> getEntities(
        cacheName: String,
        cacheType: Class<T>,
        searchIds: List<Long>,
        searchRepository: BaseRepository<T>,
    ): List<T> {
        val entityMap = getEntities(cacheName, cacheType, BaseEntity::id, searchIds, searchRepository::findAllByIdIn)
        return searchIds.filter { entityMap.containsKey(it) }.mapNotNull { entityMap[it] }.toList()
    }

    fun <T, KEY> getEntities(
        cacheName: String,
        cacheType: Class<T>,
        cacheKey: (m: T) -> KEY,
        searchKeys: Collection<KEY>,
        searchRepository: (nonExistsIds: List<KEY>) -> Collection<T>,
    ): Map<KEY, T> {
        if (searchKeys.isEmpty()) {
            return emptyMap()
        }

        val cache = cacheManager.getCache(cacheName)!!
        val cacheKeys = searchKeys.map { "${redisKeyManager.chainCachePrefix}/${cacheName}::$it" }
        val cachedEntityStrings = redisTemplate.opsForValue().multiGet(cacheKeys)?.filterNotNull() ?: emptySet()
        val cachedEntityMap = cachedEntityStrings.map {
            genericJackson2JsonRedisSerializer.deserialize(it.toByteArray(), cacheType)!!
        }.associateBy { cacheKey(it) }.toMutableMap()

        val nonExistsIds = searchKeys.filter { !cachedEntityMap.containsKey(it) }
        if (nonExistsIds.isNotEmpty()) {
            searchRepository(nonExistsIds).forEach {
                val key = cacheKey(it)

                cachedEntityMap[key] = it!!
                cache.put("$key", it)
            }
        }
        return cachedEntityMap
    }
}