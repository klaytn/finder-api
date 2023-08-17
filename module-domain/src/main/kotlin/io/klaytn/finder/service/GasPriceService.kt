package io.klaytn.finder.service

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.klaytn.finder.config.ChainProperties
import io.klaytn.finder.domain.mysql.set1.Block
import io.klaytn.finder.domain.mysql.set1.GasPrice
import io.klaytn.finder.domain.mysql.set1.GasPriceRepository
import io.klaytn.finder.infra.cache.CacheName
import io.klaytn.finder.infra.cache.CacheValue
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

@Service
class GasPriceService(
    private val gasPriceCachedService: GasPriceCachedService,
    private val chainProperties: ChainProperties,
) {
    private var gasPriceCache: Cache<String, MutableList<GasPrice>> = Caffeine.newBuilder()
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .maximumSize(10)
        .build()

    fun getAll() =
        gasPriceCache.getIfPresent(CacheValue.ALL) ?:
        gasPriceCachedService.getGasPrice().also {
            gasPriceCache.put(CacheValue.ALL, it)
        }

    fun getGasPrice(blockNumber: Long, baseFeePerGas: BigDecimal?): BigDecimal =
        if(chainProperties.isDynamicFeeTarget(blockNumber))
            baseFeePerGas ?: BigDecimal.ZERO
        else
            getGasPrice(blockNumber)

    fun getGasPrice(block: Block) = getGasPrice(block.number, block.baseFeePerGas)

    fun reload() {
        gasPriceCachedService.flush()
        gasPriceCachedService.getGasPrice().also {
            gasPriceCache.put(CacheValue.ALL, it)
        }
    }

    private fun getGasPrice(blockNumber: Long): BigDecimal {
        val gasPrices = getAll()
        return gasPrices.firstOrNull {
            blockNumber in it.minBlockNumber..it.maxBlockNumber
        }?.gasPrice ?: BigDecimal.ZERO
    }
}

@Service
class GasPriceCachedService(
    private val gasPriceRepository: GasPriceRepository,
) {
    @Cacheable(cacheNames = [CacheName.GAS_PRICE], key = CacheValue.ALL, unless = "#result == null")
    fun getGasPrice(): MutableList<GasPrice> = gasPriceRepository.findAll()

    @CacheEvict(cacheNames = [CacheName.GAS_PRICE], key = CacheValue.ALL)
    fun flush() {
    }
}