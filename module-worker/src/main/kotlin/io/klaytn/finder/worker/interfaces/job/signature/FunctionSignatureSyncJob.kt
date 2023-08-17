package io.klaytn.finder.worker.interfaces.job.signature

import io.klaytn.finder.worker.infra.client.FinderPrivateApiClient
import io.klaytn.finder.worker.infra.client.Signature
import io.klaytn.finder.worker.infra.client.SignatureClient
import io.klaytn.finder.worker.infra.client.SignatureType
import io.klaytn.finder.worker.infra.redis.RedisKeyManagerForWorker
import io.klaytn.finder.worker.infra.redis.RedisLockUtilsForWorker
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class FunctionSignatureSyncJob(
    client: SignatureClient,
    redisTemplate: RedisTemplate<String, String>,
    redisLockUtilsForWorker: RedisLockUtilsForWorker,
    redisKeyManagerForWorker: RedisKeyManagerForWorker,
    finderCypressPrivateApiClient: FinderPrivateApiClient,
    finderBaobabPrivateApiClient: FinderPrivateApiClient
) : SignatureSyncJob(
    client, redisTemplate, redisLockUtilsForWorker, redisKeyManagerForWorker, finderCypressPrivateApiClient, finderBaobabPrivateApiClient
) {
    private val jobName = "job/${this::class.java.simpleName}"

    override fun getJobName() = jobName

    @Scheduled(cron = "0 0 3 * * *")
    fun sync() {
        doSync(SignatureType.FUNCTION)
    }
}
