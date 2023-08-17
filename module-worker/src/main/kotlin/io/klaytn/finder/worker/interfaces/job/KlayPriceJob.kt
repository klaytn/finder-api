package io.klaytn.finder.worker.interfaces.job

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.worker.infra.redis.RedisKeyManagerForWorker
import io.klaytn.finder.worker.infra.redis.RedisLockUtilsForWorker
import io.klaytn.finder.worker.service.CoinPriceService
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class KlayPriceJob(
    private val coinPriceService: CoinPriceService,
    private val redisTemplate: RedisTemplate<String, String>,
    private val redisLockUtilsForWorker: RedisLockUtilsForWorker,
    private val redisKeyManagerForWorker: RedisKeyManagerForWorker
) {
    private val logger = logger(this::class.java)
    private val jobName = "job/${this::class.java.simpleName}"

    @Scheduled(cron = "0 */1 * * * *")
    fun sync() {
        logger.info("[$jobName] trying to get lock")
        if (redisLockUtilsForWorker.tryLock(jobName, Duration.ofMinutes(1))) {
            try {
                logger.info("[$jobName] process started")
                process()
                logger.info("[$jobName] process ended")
            } catch (exception: Throwable) {
                logger.error(exception.message, exception)
            } finally {
                redisLockUtilsForWorker.unlock(jobName)
                logger.info("[$jobName] lock released")
            }
        } else {
            logger.info("[$jobName] fail to get lock")
        }
    }

    private fun process() {
        val usdPrice = coinPriceService.getKlayPrice("USD")
        val btcPrice = coinPriceService.getKlayPrice("BTC")

        val coinPrice = mapOf(
            "usdPrice" to usdPrice.price.toPlainString(),
            "usdMarketCap" to usdPrice.marketCap.toPlainString(),
            "usdTotalSupply" to usdPrice.circulatingSupply.toPlainString(),
            "usdPercentChange24h" to usdPrice.percentChange24h.toPlainString(),
            "btcPrice" to btcPrice.price.toPlainString()
        )

        redisTemplate.opsForHash<String, String>().putAll(redisKeyManagerForWorker.chainCommonKlayPrice, coinPrice)
        redisTemplate.convertAndSend(redisKeyManagerForWorker.chainCommonChannelKlayPrice,
            jacksonObjectMapper().writeValueAsString(coinPrice))
    }
}
