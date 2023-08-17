package io.klaytn.finder.service.caver

import com.klaytn.caver.Caver
import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.infra.cache.CacheName
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class CaverBlockService(
    private val caver: Caver,
) {
    private val logger = logger(this.javaClass)

    @Cacheable(cacheNames = [CacheName.CAVER_BLOCK_COMMITTEE], key = "#blockNumber", unless = "#result == null or #result.isEmpty()")
    fun getCommittee(blockNumber: Long) =
        try {
            val committeeResult = caver.rpc.klay.getCommittee(blockNumber).send()
            if(!committeeResult.hasError()) {
                committeeResult.result ?: emptyList<String>()
            } else {
                emptyList<String>()
            }
        } catch (exception: Exception) {
            logger.warn(exception.message, exception)
            emptyList<String>()
        }

    @Cacheable(cacheNames = [CacheName.CAVER_COUNCIL_SIZE], unless = "#result == 0")
    fun getCouncilSize(): BigInteger =
        try {
            val councilSizeResult = caver.rpc.klay.councilSize.send()
            if(!councilSizeResult.hasError()) {
                councilSizeResult.value ?: BigInteger.ZERO
            } else {
                BigInteger.ZERO
            }
        } catch (exception: Exception) {
            logger.warn(exception.message, exception)
            BigInteger.ZERO
        }

    @Cacheable(cacheNames = [CacheName.CAVER_BLOCK_REWARD], key = "#blockNumber", unless = "#result == null")
    fun getRewards(blockNumber: Long) =
        try {
            val rewardsResult = caver.rpc.klay.getRewards(BigInteger.valueOf(blockNumber)).send()
            if(!rewardsResult.hasError()) {
                rewardsResult.result
            } else {
                null
            }
        } catch (exception: Exception) {
            logger.warn(exception.message, exception)
            null
        }
}
