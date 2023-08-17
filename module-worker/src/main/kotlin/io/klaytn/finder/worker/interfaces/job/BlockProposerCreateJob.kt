package io.klaytn.finder.worker.interfaces.job

import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.worker.infra.client.FinderPrivateApiClient
import io.klaytn.finder.worker.infra.client.SimpleApiResponseCallback
import io.klaytn.finder.worker.infra.redis.RedisLockUtilsForWorker
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class BlockProposerCreateJob(
    private val finderCypressPrivateApiClient: FinderPrivateApiClient,
    private val redisLockUtilsForWorker: RedisLockUtilsForWorker,
) {
    private val logger = logger(this::class.java)
    private val blockProposerSourceJobName = "job/${this::class.java.simpleName}/source"
    private val blockProposerCSVJobName = "job/${this::class.java.simpleName}/csv"

    @Scheduled(cron = "0 10 1 1 * *")
    fun blockProposerSource() {
        logger.info("[$blockProposerSourceJobName] trying to get lock")
        if (redisLockUtilsForWorker.tryLock(blockProposerSourceJobName, Duration.ofMinutes(5))) {
            try {
                logger.info("[$blockProposerSourceJobName] process started")
                val date = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMM"))
                finderCypressPrivateApiClient.blockProposerSource(date)
                    .enqueue(SimpleApiResponseCallback(blockProposerSourceJobName))
                logger.info("[$blockProposerSourceJobName] process ended")
            } catch (exception: Throwable) {
                logger.error(exception.message, exception)
            } finally {
                redisLockUtilsForWorker.unlock(blockProposerSourceJobName)
                logger.info("[$blockProposerSourceJobName] lock released")
            }
        } else {
            logger.info("[$blockProposerSourceJobName] fail to get lock")
        }
    }

    @Scheduled(cron = "0 30 1 1 * *")
    fun blockProposerCSV() {
        logger.info("[$blockProposerCSVJobName] trying to get lock")
        if (redisLockUtilsForWorker.tryLock(blockProposerCSVJobName, Duration.ofMinutes(5))) {
            try {
                logger.info("[$blockProposerCSVJobName] process started")
                val date = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMM"))
                finderCypressPrivateApiClient.blockProposerCSV(date)
                    .enqueue(SimpleApiResponseCallback(blockProposerCSVJobName))
                logger.info("[$blockProposerCSVJobName] process ended")
            } catch (exception: Throwable) {
                logger.error(exception.message, exception)
            } finally {
                redisLockUtilsForWorker.unlock(blockProposerCSVJobName)
                logger.info("[$blockProposerCSVJobName] lock released")
            }
        } else {
            logger.info("[$blockProposerCSVJobName] fail to get lock")
        }
    }
}