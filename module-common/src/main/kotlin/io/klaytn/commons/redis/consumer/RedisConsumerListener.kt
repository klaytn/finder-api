package io.klaytn.commons.redis.consumer

import io.klaytn.commons.utils.logback.logger
import org.springframework.beans.factory.DisposableBean
import java.util.concurrent.*
import java.util.stream.IntStream

/**
 * Inject the [RedisConsumer] implementation into the creation of [RedisConsumerListener],
 * and create a [RedisConsumerListenerManager] for lifecycle management of [RedisConsumerListener].
 * - If multiple [RedisConsumer] configurations are needed for each [RedisConsumerListener],
 *   you can set the concurrentCount in the constructor's last parameter of [RedisConsumerListener.RedisListener].
 *
 *
 * example)
 *
 * @Bean public RedisListener shoppingItemSyncRedisListener(StringRedisTemplate shoppingProductSyncRedisTemplate,
 * GiftProductRabbitMqProducer giftProductRabbitMqProducer) {
 * ShoppingProductSyncRedisConsumer shoppingProductSyncRedisConsumer new ShoppingProductSyncRedisConsumer(
 * shoppingProductSyncRedisTemplate, giftProductRabbitMqProducer, ShoppingProductSyncRedisConsumerType.ITEM, nextIdWaitTimeoutMs);
 * return new RedisListener(shoppingProductSyncRedisConsumer, shutdownWaitTimeoutMs);
 * }
 * @Bean public RedisListenerManager<GiftProductFunctionSwitchType> giftRedisListenerManager(
 * FunctionSwitchManager<GiftProductFunctionSwitchType> giftProductFunctionSwitchManager) {
 * return new RedisListenerManager<>(giftProductFunctionSwitchManager, GiftProductFunctionSwitchType.SHOPPING_PRODUCT_SYNC_FROM_REDIS);
 * }
</GiftProductFunctionSwitchType></GiftProductFunctionSwitchType> */
class RedisConsumerListener(
    private val redisConsumer: RedisConsumer,
    private val shutdownWaitTimeMs: Long,
    private val concurrentThreadCount: Int = 1,
) : DisposableBean {
    private val log = logger(this::class.java)

    private val redisListenerStatus: RedisListenerStatus = RedisListenerStatus()
    private val executorService: ExecutorService = Executors.newFixedThreadPool(concurrentThreadCount)
    private var futures: MutableList<Future<Boolean>> = mutableListOf()

    init {
        redisConsumer.redisListenerStatus = redisListenerStatus
    }

    fun start() {
        if (redisListenerStatus.running) {
            log.warn("RedisConsumer({}) is already running.", this)
            return
        }

        log.info("RedisConsumer({}) is staring.", this)
        redisListenerStatus.start()
        futures.clear()

        IntStream.range(1, concurrentThreadCount + 1).forEach { i: Int ->
            log.info("RedisConsumerCallable({}) {}/{} is submitting.", this, i, this.concurrentThreadCount)
            futures.add(executorService.submit(redisConsumer))
        }
        log.info("RedisConsumer({}) is started.", this)
    }

    fun stop() {
        if (!redisListenerStatus.stop()) {
            return
        }

        log.info("RedisConsumer({}) is stopping.", this)
        if (futures.isNotEmpty()) {
            for (future in futures) {
                try {
                    log.info("- RedisConsumerCallable({}) is stopping.", this)
                    future.get(shutdownWaitTimeMs, TimeUnit.MILLISECONDS)
                    log.info("- RedisConsumerCallable({}) is stopped.", this)
                } catch (except: Exception) {
                    log.warn("RedisConsumerCallable({}) stop is failed. caused by:{}", this, except.message, except)
                    future.cancel(true)
                }
            }
            futures.clear()
        }

        log.info("RedisConsumer({}) is stopped.", this)
    }

    override fun destroy() {
        log.info("RedisConsumer({}) is destroying.", this)
        try {
            executorService.shutdown()
            executorService.awaitTermination(shutdownWaitTimeMs, TimeUnit.MILLISECONDS)
            log.info("RedisConsumer({}) is destroyed.", this)
        } catch (except: Exception) {
            log.warn("RedisConsumer({}) destroy is failed. caused by:{}", this, except.message, except)
        }
    }
}