package io.klaytn.finder.service.nft

import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.config.ChainProperties
import io.klaytn.finder.config.dynamic.FinderServerNftRefreshConfig
import io.klaytn.finder.domain.redis.NftTokenUriRefreshRequest
import io.klaytn.finder.infra.exception.NftTokenUriRefreshRequestLimitedException
import io.klaytn.finder.infra.redis.RedisKeyManager
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class NftInventoryRefreshRequestService(
    private val redisKeyManager: RedisKeyManager,
    private val chainProperties: ChainProperties,
    private val redisTemplate: RedisTemplate<String, String>,
    private val nftTokenUriRefreshRequestRedisTemplate: RedisTemplate<String, NftTokenUriRefreshRequest>,
    private val finderServerNftRefreshConfig: FinderServerNftRefreshConfig
) {
    private val logger = logger(this::class.java)

    // -- --------------------------------------------------------------------------------------------------------------
    // -- Update tokenUri for nftAddress#tokenId
    // -- --------------------------------------------------------------------------------------------------------------

    /**
     * Requests an update for tokenUri of nftAddress#tokenId.
     */
    fun refreshNftTokenUri(nftAddress: String, tokenId: String): Boolean {
        val limiterKey = getRefreshNftTokenUriLimiterKey(nftAddress, tokenId)
        val limiterTtl = Duration.ofSeconds(finderServerNftRefreshConfig.tokenUriRefreshLockTime)
        if (redisTemplate.opsForValue().setIfAbsent(limiterKey, "1", limiterTtl) == false) {
            throw NftTokenUriRefreshRequestLimitedException()
        }

        val boundListOps =
            nftTokenUriRefreshRequestRedisTemplate.boundListOps(redisKeyManager.workerRedisConsumerQueue("nft_token_uri_refresh_requests"))
        val nftTokenUriContentRefreshRequest = NftTokenUriRefreshRequest(chainProperties.type, nftAddress, tokenId)
        boundListOps.leftPush(nftTokenUriContentRefreshRequest)
        return true
    }

    fun existsRefreshNftTokenUriLimiter(nftAddress: String, tokenId: String): Boolean {
        val limiterKey = getRefreshNftTokenUriLimiterKey(nftAddress, tokenId)
        return redisTemplate.hasKey(limiterKey)
    }

    fun deleteRefreshNftTokenUriLimiter(nftAddress: String, tokenId: String): Boolean {
        val limiterKey = getRefreshNftTokenUriLimiterKey(nftAddress, tokenId)
        return redisTemplate.delete(limiterKey)
    }

    fun getRefreshNftTokenUriLimiterKey(nftAddress: String, tokenId: String) =
        redisKeyManager.chainNftTokenUriRefreshLimiter(nftAddress, tokenId)
}