package io.klaytn.finder.service

import io.klaytn.finder.domain.mysql.set1.*
import io.klaytn.finder.infra.cache.CacheName
import io.klaytn.finder.infra.cache.CacheUtils
import org.springframework.stereotype.Service

@Service
class BlockRewardService(
    private val blockRewardCachedService: BlockRewardCachedService,
) {
    fun getBlockReward(number: Long) =
        blockRewardCachedService.getBlockReward(number)

    fun getBlockRewards(searchNumbers: List<Long>) =
        blockRewardCachedService.getBlockRewards(searchNumbers)
}

@Service
class BlockRewardCachedService(
    private val blockRewardRepository: BlockRewardRepository,
    private val cacheUtils: CacheUtils,
) {
    fun getBlockReward(number: Long): BlockReward? {
        val blockRewards = getBlockRewards(listOf(number))
        return if (blockRewards.size == 1) {
            blockRewards[number]
        } else {
            null
        }
    }

    fun getBlockRewards(searchNumbers: List<Long>) =
        cacheUtils.getEntities(
            CacheName.BLOCK_REWARD_BY_NUMBER,
            BlockReward::class.java,
            BlockReward::number,
            searchNumbers,
            blockRewardRepository::findAllByNumberIn)
}