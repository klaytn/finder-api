package io.klaytn.finder.infra.redis

import io.klaytn.finder.config.ChainProperties
import org.springframework.stereotype.Component

@Component
class RedisKeyManager(val chainProperties: ChainProperties) {
    // -- --------------------------------------------------------------------------------------------------------------
    // -- common
    // -- --------------------------------------------------------------------------------------------------------------

    val chainCommonPrefix: String
        get() = "finder/common"

    val chainCommonChannelPattern: String
        get() = "${chainCommonPrefix}/channel:*"

    val chainCommonChannelKlayPrice: String
        get() = "${chainCommonPrefix}/channel:klay-price"

    val chainCommonKlayPrice: String
        get() = "${chainCommonPrefix}/klay:price"

    fun chainCommonSignature(signatureType: String, bytes: String) =
        "$chainCommonPrefix/signature/$signatureType:$bytes"

    // -- --------------------------------------------------------------------------------------------------------------
    // -- chain
    // -- --------------------------------------------------------------------------------------------------------------

    val chainPrefix: String
        get() = "finder/${chainProperties.type}"

    val chainChannelPattern: String
        get() = "${chainPrefix}/channel:*"

    val chainChannelBlock: String
        get() = "${chainPrefix}/channel:block"

    val chainChannelInternalTx: String
        get() = "${chainPrefix}/channel:internal-tx"

    val chainStatAvgBlockTime: String
        get() = "${chainPrefix}/stat:AvgBlockTime:24h"

    val chainStatAvgTxPerBlock: String
        get() = "${chainPrefix}/stat:AvgTxPerBlock:24h"

    val chainStatTransactionHistory: String
        get() = "${chainPrefix}/stat:TransactionHistory:30days"

    val chainStatBurntByGasFeeHistory: String
        get() = "${chainPrefix}/stat:BurntByGasFeeHistory:30days"

    fun chainLatestBlock(type: String) =
        "$chainPrefix/latest:$type"

    fun chainBlockTime(blockNumber: Long) =
        "$chainPrefix/block:$blockNumber"

    fun chainRedisLock(type: String) =
        "$chainPrefix/lock/$type"

    fun chainRateLimiter(type: String) =
        "$chainPrefix/rate_limiter/$type"

    fun chainNftTokenUriRefreshLimiter(contractAddress: String, tokenId: String) =
        "$chainPrefix/nft_token_uri_refresh_limiter/$contractAddress/$tokenId"

    // -- --------------------------------------------------------------------------------------------------------------
    // -- chain cache
    // -- --------------------------------------------------------------------------------------------------------------

    val chainCachePrefix: String
        get() = "${chainPrefix}/cache"

    // -- --------------------------------------------------------------------------------------------------------------
    // -- worker
    // -- --------------------------------------------------------------------------------------------------------------

    val workerPrefix: String
        get() = "finder-worker"

    fun workerRedisConsumerQueue(queueName: String) =
        "$workerPrefix/redis-consumer/$queueName"

    // -- --------------------------------------------------------------------------------------------------------------
    // -- kaia users
    // -- --------------------------------------------------------------------------------------------------------------

    fun chainKaiaUserSignIn(sessionId: String) =
        "$chainPrefix/user/session:$sessionId"
}