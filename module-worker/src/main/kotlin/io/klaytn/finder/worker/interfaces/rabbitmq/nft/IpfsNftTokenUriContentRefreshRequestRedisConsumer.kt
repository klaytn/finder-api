package io.klaytn.finder.worker.interfaces.rabbitmq.nft

import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import io.ipfs.api.IPFS
import io.ipfs.multihash.Multihash
import io.klaytn.commons.redis.consumer.RedisConsumer
import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.domain.redis.NftTokenUriContentRefreshRequest
import io.klaytn.finder.worker.infra.redis.RedisKeyManagerForWorker
import org.springframework.dao.QueryTimeoutException
import org.springframework.data.redis.core.RedisTemplate

import java.time.Duration

class IpfsNftTokenUriContentRefreshRequestRedisConsumer(
    private val redisTemplate: RedisTemplate<String, NftTokenUriContentRefreshRequest>,
    private val ipfs: IPFS,
    private val gcsClient: Storage,
    private val gcsBucketName: String,
    private val redisKeyManagerForWorker: RedisKeyManagerForWorker
) : RedisConsumer() {
    private val logger = logger(this::class.java)

    override fun call(): Boolean {
        val boundListOps = redisTemplate.boundListOps(redisKeyManagerForWorker.workerRedisConsumerQueue("ifps_nft_token_uris"))

        while (true) {
            try {
                logger.debug("waiting message....")
                boundListOps.leftPop(Duration.ofSeconds(3))?.let { nftTokenRequest ->
                    logger.info("received message : $nftTokenRequest")

                    val cid = nftTokenRequest.tokenUri.substringAfter("ipfs://")
                    cat(cid)?.let {
                        logger.info("[download] : $nftTokenRequest.tokenUri")
                        val path = "finder/${nftTokenRequest.chain}/nft-inventory-contents/${nftTokenRequest.contractAddress}/${nftTokenRequest.tokenId}"
                        val blobInfo = BlobInfo.newBuilder(gcsBucketName, path).build()
                        gcsClient.create(blobInfo, it)
                        logger.info("[upload to gcs] : $nftTokenRequest.tokenUri")
                    }
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

    private fun cat(cid: String): ByteArray? {
        try {
            val filePointer = Multihash.fromBase58(cid)
            return ipfs.cat(filePointer)
        } catch (throwable: Throwable) {
            logger.warn("fail to get ipfs:$cid", throwable)
        }
        return null
    }
}