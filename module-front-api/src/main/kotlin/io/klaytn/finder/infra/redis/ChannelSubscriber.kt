package io.klaytn.finder.infra.redis

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.klaytn.finder.interfaces.rest.api.websocket.FinderHomePublisher
import io.klaytn.finder.service.BlockService
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.core.RedisTemplate

class ChannelSubscriber(
    private val blockService: BlockService,
    private val finderHomePublisher: FinderHomePublisher,
    private val redisTemplate: RedisTemplate<String, String>,
    private val redisKeyManager: RedisKeyManager
) : MessageListener {
    override fun onMessage(message: Message, pattern: ByteArray?) {
        val channel = String(message.channel)
        val body = String(message.body)

        if (channel == redisKeyManager.chainCommonChannelKlayPrice) {
            onKlayPriceMessage(body)
        } else {
            onBlockMessage(channel, body.toLong())
        }
    }

    private fun onKlayPriceMessage(body: String) {
        val klayPrice: Map<String, String> = jacksonObjectMapper().readValue(body)
        finderHomePublisher.sendKlayPrice(klayPrice)
    }

    private fun onBlockMessage(channel: String, blockNo: Long) {
        // TODO: The block_number for internal transactions should be retrieved from the sharding database.
        val blockNumberFromDb = blockService.getLatestNumber()

        val blockNumber = if (blockNo < blockNumberFromDb) blockNumberFromDb else blockNo
        val blockTimestamp = redisTemplate.opsForValue().get("block:$blockNumber")?.toInt()
            ?: blockService.getBlock(blockNumber)?.timestamp ?: 0

        when (channel) {
            redisKeyManager.chainChannelBlock ->
                finderHomePublisher.sendBlock(blockNumber, blockTimestamp)
            redisKeyManager.chainChannelInternalTx ->
                finderHomePublisher.sendInternalTx(blockNumber, blockTimestamp)
        }
    }
}
