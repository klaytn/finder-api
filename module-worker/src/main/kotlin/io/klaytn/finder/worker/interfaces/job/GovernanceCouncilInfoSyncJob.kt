package io.klaytn.finder.worker.interfaces.job

import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.worker.infra.client.FinderPrivateApiClient
import io.klaytn.finder.worker.infra.client.SimpleApiResponseCallback
import io.klaytn.finder.worker.infra.redis.RedisLockUtilsForWorker
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class GovernanceCouncilInfoSyncJob(
    private val finderCypressPrivateApiClient: FinderPrivateApiClient,
    private val redisLockUtilsForWorker: RedisLockUtilsForWorker
) {
    private val logger = logger(this::class.java)
    private val jobName = "job/${this::class.java.simpleName}"

    @Scheduled(cron = "0 0 */1 * * *")
    fun sync() {
        logger.info("[$jobName] trying to get lock")
        if (redisLockUtilsForWorker.tryLock(jobName, Duration.ofMinutes(1))) {
            try {
                logger.info("[$jobName] process started")
                finderCypressPrivateApiClient.governanceCouncilInfoSync(false)
                    .enqueue(SimpleApiResponseCallback(jobName))
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
}