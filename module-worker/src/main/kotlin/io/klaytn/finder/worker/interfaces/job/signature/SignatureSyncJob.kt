package io.klaytn.finder.worker.interfaces.job.signature

import io.klaytn.commons.utils.logback.logger
import io.klaytn.commons.utils.retrofit2.orElseThrow
import io.klaytn.finder.worker.infra.client.*
import io.klaytn.finder.worker.infra.redis.RedisKeyManagerForWorker
import io.klaytn.finder.worker.infra.redis.RedisLockUtilsForWorker
import org.springframework.data.redis.core.RedisTemplate
import java.time.Duration
import kotlin.math.max

abstract class SignatureSyncJob(
    private val client: SignatureClient,
    private val redisTemplate: RedisTemplate<String, String>,
    private val redisLockUtilsForWorker: RedisLockUtilsForWorker,
    private val redisKeyManagerForWorker: RedisKeyManagerForWorker,
    private val finderCypressPrivateApiClient: FinderPrivateApiClient,
    private val finderBaobabPrivateApiClient: FinderPrivateApiClient,
) {
    private val logger = logger(this::class.java)

    fun doSync(signatureType: SignatureType) {
        val jobName = getJobName()

        logger.info("[$jobName] trying to get lock....")
        if (redisLockUtilsForWorker.tryLock(jobName, Duration.ofHours(2))) {
            try {
                logger.info("[$jobName] process started")
                process(signatureType)
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

    private fun process(type: SignatureType) {
        val jobName = getJobName()

        var page = 1

        val lastId = redisTemplate.opsForValue().get(redisKeyManagerForWorker.chainCommonSignature(type.key, "id"))?.toInt() ?: 1
        var maxId = lastId

        while (true) {
            val response =
                if (type == SignatureType.FUNCTION)
                    client.getSignatures(page).orElseThrow { message -> IllegalStateException(message) }
                else client.getEventSignatures(page).orElseThrow { message -> IllegalStateException(message) }

            page += 1

            response.results.forEach {
                logger.debug("[$jobName] add $type.key:$it.hexSignature")

                if(type == SignatureType.FUNCTION) {
                    finderCypressPrivateApiClient
                        .addFunctionSignature(it.id.toLong(), it.hexSignature, it.textSignature)
                        .enqueue(FunctionSignatureCallback(type, "cypress"))
                    finderBaobabPrivateApiClient
                        .addFunctionSignature(it.id.toLong(), it.hexSignature, it.textSignature)
                        .enqueue(FunctionSignatureCallback(type, "baobab"))
                } else if(type == SignatureType.EVENT) {
                    finderCypressPrivateApiClient
                        .addEventSignature(it.id.toLong(), it.hexSignature, it.textSignature)
                        .enqueue(EventSignatureCallback(type, "cypress"))
                    finderBaobabPrivateApiClient
                        .addEventSignature(it.id.toLong(), it.hexSignature, it.textSignature)
                        .enqueue(EventSignatureCallback(type, "baobab"))
                }
            }

            logger.info("[$jobName] added ${response.results.size}")

            if (response.results.isNotEmpty()) {
                maxId = max(maxId, response.results.maxOf { it.id })
            }

            if (response.results.any { it.id == lastId }) {
                logger.info("[$jobName] all data synced.")
                break
            }

            if (response.next == null) {
                logger.info("[$jobName] all data synced.")
                break
            }
        }
        redisTemplate.opsForValue().set(redisKeyManagerForWorker.chainCommonSignature(type.key, "id"), maxId.toString())
    }

    abstract fun getJobName(): String
}