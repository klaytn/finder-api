package io.klaytn.finder.worker.infra.redis

import java.time.Duration
import org.springframework.data.redis.core.RedisTemplate

class RedisLockUtilsForWorker(
        private val redisTemplate: RedisTemplate<String, String>,
        private val redisKeyManagerForWorker: RedisKeyManagerForWorker
) {
    fun tryLock(key: String, timeout: Duration) =
            redisTemplate
                    .opsForValue()
                    .setIfAbsent(
                            redisKeyManagerForWorker.workerRedisLock(key),
                            "${System.currentTimeMillis()}",
                            timeout
                    )!!
    fun unlock(key: String) = redisTemplate.delete(redisKeyManagerForWorker.workerRedisLock(key))
}
