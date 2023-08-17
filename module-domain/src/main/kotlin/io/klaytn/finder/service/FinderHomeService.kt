package io.klaytn.finder.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.klaytn.finder.config.dynamic.FinderServerFeatureConfig
import io.klaytn.finder.infra.redis.RedisKeyManager
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.infra.utils.KlayUtils
import io.klaytn.finder.service.caver.CaverBlockService
import io.klaytn.finder.view.mapper.BlockBurntToViewMapper
import io.klaytn.finder.view.model.*
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Service
class FinderHomeService(
    private val blockService: BlockService,
    private val transactionService: TransactionService,
    private val redisTemplate: RedisTemplate<String, String>,
    private val caverBlockService: CaverBlockService,
    private val objectMapper: ObjectMapper,
    private val redisKeyManager: RedisKeyManager,
    private val blockBurntToViewMapper: BlockBurntToViewMapper,
    private val finderServerFeatureConfig: FinderServerFeatureConfig,
) {
    private val defaultKlayPrice = "0.0"

    fun getSummary() = FinderSummary(
        consensusNode = caverBlockService.getCouncilSize().toInt(),
        averageBlockTime = "${redisTemplate.opsForValue().get(redisKeyManager.chainStatAvgBlockTime) ?: "0"}s",
        averageTxPerBlock = redisTemplate.opsForValue().get(redisKeyManager.chainStatAvgTxPerBlock)?.toInt() ?: 0,
        transactionPerSec = getTransactionsPerSec()
    )

    fun getTransactionHistory(): FinderTransactionHistory {
        val txHistoryMapJson = redisTemplate.opsForValue().get(redisKeyManager.chainStatTransactionHistory)
        val transactionCounts = txHistoryMapJson?.let {
            objectMapper.readValue(txHistoryMapJson, object : TypeReference<Map<LocalDate, Long>>() {})
                .toSortedMap().filterNot { it.key == LocalDate.now() }.map { FinderTransactionCount(it.key, it.value) }
        } ?: emptyList()

        return FinderTransactionHistory(
            transactionService.getMaxTransactionId().toBigDecimal(),
            transactionCounts.takeLast(15))
    }

    fun getBurntByGasFeeHistory(): FinderBurntByGasFeeHistory {
        val burntByGasFeeHistoryMapJson = redisTemplate.opsForValue().get(redisKeyManager.chainStatBurntByGasFeeHistory)
        val burntByGasFeeHistories = burntByGasFeeHistoryMapJson?.let {
            objectMapper.readValue(burntByGasFeeHistoryMapJson, object : TypeReference<Map<LocalDate, BigDecimal>>() {})
                .toSortedMap()
                .filterNot { it.key == LocalDate.now() }
                .map {
                    FinderBurntByGasFee(it.key, KlayUtils.pebToKlay(it.value.toPlainString()))
                }
        } ?: emptyList()

        return FinderBurntByGasFeeHistory(burntByGasFeeHistories.takeLast(15))
    }

    fun getStatus() = getBlockNoAndTimestamp("block").let {
        getStatus(it.first, it.second)
    }

    fun getStatus(blockNo: Long, blockTimestamp: Int): FinderStatus {
        val blockBurnView = blockService.getBlockBurn(blockNo)?.let {
            blockBurntToViewMapper.transform(it)
        }

        return FinderStatus(
            blockHeight = blockNo,
            datetime = DateUtils.timestampToLocalDateTime(blockTimestamp),
            blockBurnt = blockBurnView
        )
    }

    fun getKlayPrice() =
        getKlayPrice(redisTemplate.opsForHash<String, String>().entries(redisKeyManager.chainCommonKlayPrice))

    fun getKlayPrice(klayPrice: Map<String, String>) = FinderKlayPrice(
        usdPrice = BigDecimal(klayPrice["usdPrice"] ?: defaultKlayPrice),
        btcPrice = BigDecimal(klayPrice["btcPrice"] ?: defaultKlayPrice),
        usdPriceChanges = BigDecimal(klayPrice["usdPercentChange24h"] ?: defaultKlayPrice),
        marketCap = BigDecimal(klayPrice["usdMarketCap"] ?: defaultKlayPrice),
        totalSupply = BigDecimal(klayPrice["usdTotalSupply"] ?: defaultKlayPrice),
    )

    private fun getBlockNoAndTimestamp(type: String): Pair<Long, Int> {
        val blockNumberFromRedis = redisTemplate.opsForValue().get(redisKeyManager.chainLatestBlock(type))?.toLong() ?: 0L
        val blockNumberFromDb = blockService.getLatestNumber()

        val blockNumber = if (blockNumberFromRedis < blockNumberFromDb) blockNumberFromDb else blockNumberFromRedis
        var blockTimestamp = redisTemplate.opsForValue().get(redisKeyManager.chainBlockTime(blockNumber))?.toInt()
        if (blockTimestamp == null) {
            val block = blockService.getBlock(blockNumber)
            blockTimestamp = block?.timestamp ?: 0
        }
        return Pair(blockNumber, blockTimestamp)
    }

    private fun getTransactionsPerSec(): Long {
        val transactionPerSecCheckUnit = finderServerFeatureConfig.transactionPerSecCheckUnit
        return if("hour".equals(transactionPerSecCheckUnit, true)) {
            val currentDate = LocalDateTime.now().withSecond(0).withNano(0).plusMinutes(1)
            val currentTimestamp = DateUtils.localDateTimeToEpochMilli(currentDate) / 1000

            val internal = TimeUnit.HOURS.toSeconds(1)
            val totalTransactionCount =
                blockService.getTotalTransactionCountByTimestamp(currentTimestamp-internal, currentTimestamp)
            if(totalTransactionCount > 0) {
                totalTransactionCount / internal
            } else {
                0
            }
        } else {
            val currentTimestamp = System.currentTimeMillis()/1000
            blockService.getTotalTransactionCountByTimestamp(currentTimestamp-2, currentTimestamp-1)
        }
    }
}