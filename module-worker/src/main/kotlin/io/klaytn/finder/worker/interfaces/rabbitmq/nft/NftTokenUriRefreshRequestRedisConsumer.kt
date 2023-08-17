package io.klaytn.finder.worker.interfaces.rabbitmq.nft

import io.klaytn.commons.redis.consumer.RedisConsumer
import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.domain.redis.NftTokenUriRefreshRequest
import io.klaytn.finder.worker.infra.client.FinderPrivateApiClient
import io.klaytn.finder.worker.infra.client.SimpleApiResponseCallback
import io.klaytn.finder.worker.infra.redis.RedisKeyManagerForWorker
import org.springframework.dao.QueryTimeoutException
import org.springframework.data.redis.core.RedisTemplate
import java.time.Duration

class NftTokenUriRefreshRequestRedisConsumer(
    private val redisTemplate: RedisTemplate<String, NftTokenUriRefreshRequest>,
    private val redisKeyManagerForWorker: RedisKeyManagerForWorker,
    private val finderCypressPrivateApiClient: FinderPrivateApiClient,
    private val finderBaobabPrivateApiClient: FinderPrivateApiClient
) : RedisConsumer() {
    private val logger = logger(this::class.java)

    override fun call(): Boolean {
        val boundListOps = redisTemplate.boundListOps(redisKeyManagerForWorker.workerRedisConsumerQueue("nft_token_uri_refresh_requests"))

        while (true) {
            try {
                logger.debug("waiting message....")
                boundListOps.leftPop(Duration.ofSeconds(3))?.let { nftTokenUriRefreshRequest ->
                    logger.info("[$nftTokenUriRefreshRequest] received.")

                    val refreshNftItemCallback =
                        with(nftTokenUriRefreshRequest) {
                            SimpleApiResponseCallback("$chain/$contractAddress/$tokenId")
                        }

                    if(nftTokenUriRefreshRequest.chain.equals("cypress", ignoreCase = true)) {
                        finderCypressPrivateApiClient.refreshNftItem(
                            nftAddress = nftTokenUriRefreshRequest.contractAddress,
                            tokenId = nftTokenUriRefreshRequest.tokenId,
                            batchSize = 100
                        ).enqueue(refreshNftItemCallback)
                    } else if(nftTokenUriRefreshRequest.chain.equals("baobab", ignoreCase = true)) {
                        finderBaobabPrivateApiClient.refreshNftItem(
                            nftAddress = nftTokenUriRefreshRequest.contractAddress,
                            tokenId = nftTokenUriRefreshRequest.tokenId,
                            batchSize = 100
                        ).enqueue(refreshNftItemCallback)
                    }
                    logger.info("[$nftTokenUriRefreshRequest] processed.")
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