package io.klaytn.finder.worker.config

import io.ipfs.api.IPFS
import io.klaytn.commons.redis.consumer.RedisConsumerListener
import io.klaytn.commons.redis.consumer.RedisConsumerListenerManager
import io.klaytn.finder.config.FinderS3Properties
import io.klaytn.finder.domain.redis.NftTokenUriContentRefreshRequest
import io.klaytn.finder.domain.redis.NftTokenUriRefreshRequest
import io.klaytn.finder.worker.infra.client.FinderPrivateApiClient
import io.klaytn.finder.worker.infra.redis.RedisKeyManagerForWorker
import io.klaytn.finder.worker.interfaces.rabbitmq.nft.*
import io.klaytn.finder.worker.interfaces.rabbitmq.solidity.SolidifyCompilerUploadRedisConsumer
import io.klaytn.finder.worker.interfaces.rabbitmq.solidity.SolidityCompilerUploadRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.web.client.RestTemplate
import software.amazon.awssdk.services.s3.S3Client

@Configuration
class RedisConsumerConfig {
    @Bean
    fun nftTokenUriRefreshRequestRedisConsumerListener(
        redisTemplate: RedisTemplate<String, NftTokenUriRefreshRequest>,
        redisKeyManagerForWorker: RedisKeyManagerForWorker,
        finderCypressPrivateApiClient: FinderPrivateApiClient,
        finderBaobabPrivateApiClient: FinderPrivateApiClient
    ) =
        RedisConsumerListener(
            redisConsumer = NftTokenUriRefreshRequestRedisConsumer(redisTemplate,
                redisKeyManagerForWorker,
                finderCypressPrivateApiClient,
                finderBaobabPrivateApiClient),
            shutdownWaitTimeMs = 5000,
            concurrentThreadCount = 5)

    @Bean
    fun httpNftTokenUriRequestRedisConsumerListener(
        redisTemplate: RedisTemplate<String, NftTokenUriContentRefreshRequest>,
        restTemplate: RestTemplate,
        s3Client: S3Client,
        finderS3Properties: FinderS3Properties,
        redisKeyManagerForWorker: RedisKeyManagerForWorker,
    ) =
        RedisConsumerListener(
            redisConsumer = HttpNftTokenUriContentRefreshRequestRedisConsumer(redisTemplate,
                restTemplate,
                s3Client,
                finderS3Properties.privateBucket,
                redisKeyManagerForWorker),
            shutdownWaitTimeMs = 5000,
            concurrentThreadCount = 5)

//    @Bean
    fun ipfsNftTokenUriRequestRedisConsumerListener(
    redisTemplate: RedisTemplate<String, NftTokenUriContentRefreshRequest>,
    ipfs: IPFS,
    s3Client: S3Client,
    finderS3Properties: FinderS3Properties,
    redisKeyManagerForWorker: RedisKeyManagerForWorker,
    ) =
        RedisConsumerListener(
            redisConsumer = IpfsNftTokenUriContentRefreshRequestRedisConsumer(redisTemplate,
                ipfs,
                s3Client,
                finderS3Properties.privateBucket,
                redisKeyManagerForWorker),
            shutdownWaitTimeMs = 5000,
            concurrentThreadCount = 5)

    @Bean
    fun solidifyCompilerUploadRedisConsumerListener(
        redisTemplate: RedisTemplate<String, SolidityCompilerUploadRequest>,
        restTemplate: RestTemplate,
        s3Client: S3Client,
        finderS3Properties: FinderS3Properties,
        redisKeyManagerForWorker: RedisKeyManagerForWorker,
    ) =
        RedisConsumerListener(
            redisConsumer = SolidifyCompilerUploadRedisConsumer(redisTemplate,
                restTemplate,
                s3Client,
                finderS3Properties.privateBucket,
                redisKeyManagerForWorker),
            shutdownWaitTimeMs = 5000,
            concurrentThreadCount = 5)

    @Bean
    fun redisConsumerListenerManager(redisConsumerListeners: List<RedisConsumerListener>) =
        RedisConsumerListenerManager(redisConsumerListeners)
}


