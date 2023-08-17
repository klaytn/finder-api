package io.klaytn.finder.worker.interfaces.rabbitmq.nft

import io.klaytn.commons.redis.consumer.RedisConsumer
import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.domain.redis.NftTokenUriContentRefreshRequest
import io.klaytn.finder.worker.infra.redis.RedisKeyManagerForWorker
import org.springframework.dao.QueryTimeoutException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.time.Duration

class HttpNftTokenUriContentRefreshRequestRedisConsumer(
    private val redisTemplate: RedisTemplate<String, NftTokenUriContentRefreshRequest>,
    private val restTemplate: RestTemplate,
    private val s3Client: S3Client,
    private val s3BucketName: String,
    private val redisKeyManagerForWorker: RedisKeyManagerForWorker
) : RedisConsumer() {
    private val logger = logger(this::class.java)
    private val ignoreAddress = listOf("127.0.0.1", "localhost")

    override fun call(): Boolean {
        val boundListOps = redisTemplate.boundListOps(redisKeyManagerForWorker.workerRedisConsumerQueue("http_nft_token_uris"))

        while (true) {
            try {
                logger.debug("waiting message....")
                boundListOps.leftPop(Duration.ofSeconds(3))?.let { nftTokenRequest ->
                    logger.debug("received message : $nftTokenRequest")

                    if (nftTokenRequest.tokenUri.indexOfAny(ignoreAddress, 0, true) == -1) {
                        restTemplate.execute(nftTokenRequest.tokenUri, HttpMethod.GET, null, { response ->
                            if(response.statusCode.is2xxSuccessful) {
                                logger.info("[download] : $nftTokenRequest.tokenUri")

                                val objectRequest = PutObjectRequest.builder()
                                    .bucket(s3BucketName)
                                    .key("finder/${nftTokenRequest.chain}/nft-inventory-contents/${nftTokenRequest.contractAddress}/${nftTokenRequest.tokenId}")
                                    .build()

                                s3Client.putObject(objectRequest, RequestBody.fromBytes(response.body.readBytes()))
                                logger.info("[upload to s3] : $nftTokenRequest.tokenUri")
                            } else {
                                logger.warn("[download fail] : $nftTokenRequest.tokenUri")
                            }
                        })
                    }
                }
            } catch (queryTimeoutException: QueryTimeoutException) {
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