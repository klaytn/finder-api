package io.klaytn.finder.worker.interfaces.rabbitmq.solidity

import io.klaytn.commons.redis.consumer.RedisConsumer
import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.worker.infra.redis.RedisKeyManagerForWorker
import org.springframework.dao.QueryTimeoutException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate
import java.time.Duration
import com.google.cloud.storage.Storage
import com.google.cloud.storage.BlobInfo



class SolidifyCompilerUploadRedisConsumer(
    private val redisTemplate: RedisTemplate<String, SolidityCompilerUploadRequest>,
    private val restTemplate: RestTemplate,
    private val gcsClient: Storage,
    private val gcsBucketName: String,
    private val redisKeyManagerForWorker: RedisKeyManagerForWorker
) : RedisConsumer() {
    private val logger = logger(this::class.java)

    override fun call(): Boolean {
        val boundListOps = redisTemplate.boundListOps(redisKeyManagerForWorker.workerRedisConsumerQueue("solidity_compiler_uploads"))

        while (true) {
            try {
                logger.debug("waiting message....")
                boundListOps.leftPop(Duration.ofSeconds(3))?.let { solidityCompilerUploadRequest ->
                    logger.info("received message : $solidityCompilerUploadRequest")

                    val osPath = solidityCompilerUploadRequest.osPath
                    val buildPath = solidityCompilerUploadRequest.buildPath

                    val url = "https://github.com/ethereum/solc-bin/raw/gh-pages/$osPath/$buildPath"
                    restTemplate.execute(url, HttpMethod.GET, null, { response ->
                        if(response.statusCode.is2xxSuccessful) {
                            logger.info("[download from git] : $url")
                            val path = "compiler/$osPath/$buildPath"
                            val blobInfo = BlobInfo.newBuilder(gcsBucketName, path).build()
                            gcsClient.create(blobInfo, response.body)
                            logger.info("[upload to s3] : $url")
                        } else {
                            logger.warn("[download fail from git] : $url")
                        }
                    })
                }
            } catch (queryTimeoutException: QueryTimeoutException) {
                logger.info(queryTimeoutException.message)
                logger.debug(queryTimeoutException.message, queryTimeoutException)
            } catch (throwable: Throwable) {
                logger.error(throwable.message, throwable)
            }

            if (redisListenerStatus.stopped) {
                break;
            }
        }
        return true;
    }
}