package io.klaytn.finder.worker.infra.redis

import org.springframework.stereotype.Component

@Component
class RedisKeyManagerForWorker {
    // --
    // --------------------------------------------------------------------------------------------------------------
    // -- common
    // --
    // --------------------------------------------------------------------------------------------------------------

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

    // --
    // --------------------------------------------------------------------------------------------------------------
    // -- worker
    // --
    // --------------------------------------------------------------------------------------------------------------

    val workerPrefix: String
        get() = "finder-worker"

    fun workerRedisLock(type: String) = "$workerPrefix/lock/$type"

    fun workerRedisConsumerQueue(queueName: String) = "$workerPrefix/redis-consumer/$queueName"
}
