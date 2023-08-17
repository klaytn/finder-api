package io.klaytn.commons.redis.consumer

import java.util.concurrent.Callable

/**
 * Recommendation: It is advisable to implement this in a thread-safe manner.
 *
 * If the [RedisConsumerListener] has been configured with a concurrentThreadCount greater than 1,
 * and the [RedisConsumer] is not inherently multithreaded, potential issues may arise.
 * In such cases, while declaring [RedisConsumer], the Bean's scope should be set to prototype.
 */
abstract class RedisConsumer : Callable<Boolean> {
    lateinit var redisListenerStatus: RedisListenerStatus
    abstract override fun call(): Boolean
}
