package io.klaytn.finder.infra.security

import io.klaytn.finder.infra.exception.ApiLimitExceededException
import io.klaytn.finder.infra.exception.DuplicatedApiRequestException
import io.klaytn.finder.infra.redis.RedisKeyManager
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class FinderApiRateLimiter(
    private val redisTemplate: RedisTemplate<String, String>,
    private val redisKeyManager: RedisKeyManager
) {
    private val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    fun checkLimit(id: Long, nonce: String, limit: Long) {
        val key = redisKeyManager.chainRateLimiter("api:$id.${formatter.format(LocalDate.now())}")
        val value = redisTemplate.opsForValue().increment(key) ?: 1L

        if (value == 1L) {
            redisTemplate.expire(key, Duration.ofDays(1L))
        }

        if (value >= limit) {
            throw ApiLimitExceededException()
        }

        val nonceKey = redisKeyManager.chainRateLimiter("api:nonce:$id:$nonce")
        if (redisTemplate.opsForValue().setIfAbsent(nonceKey, "1", Duration.ofDays(1L)) == false) {
            throw DuplicatedApiRequestException()
        }
    }
}
