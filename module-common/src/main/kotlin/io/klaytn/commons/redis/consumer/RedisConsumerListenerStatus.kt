package io.klaytn.commons.redis.consumer

import java.util.concurrent.atomic.AtomicReference

data class RedisListenerStatus(
        val status: AtomicReference<RedisListenerStatusType> =
                AtomicReference(RedisListenerStatusType.NONE),
) {
    val running: Boolean
        get() = status.get() == RedisListenerStatusType.RUNNING

    val stopped: Boolean
        get() = status.get() == RedisListenerStatusType.STOPPED

    fun stop() =
            status.compareAndSet(RedisListenerStatusType.RUNNING, RedisListenerStatusType.STOPPED)

    fun start() = status.set(RedisListenerStatusType.RUNNING)
}

enum class RedisListenerStatusType {
    NONE,
    RUNNING,
    STOPPED
}
