package io.klaytn.finder.service

import io.klaytn.finder.config.dynamic.FinderServerPaging
import io.klaytn.finder.domain.mysql.set1.Block
import io.klaytn.finder.domain.mysql.set1.BlockBurnRepository
import io.klaytn.finder.domain.mysql.set1.BlockRepository
import io.klaytn.finder.infra.cache.CacheName
import io.klaytn.finder.infra.cache.CacheUtils
import io.klaytn.finder.infra.utils.PageUtils
import io.klaytn.finder.infra.web.model.SimplePageRequest
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class BlockService(
    private val blockCachedService: BlockCachedService,
    private val blockBurnCachedService: BlockBurnCachedService,
) {
    fun getBlocks(
        blockNumberRange: LongRange? = null,
        simplePageRequest: SimplePageRequest
    ): Page<Block> {
        val count =
            if(blockNumberRange == null) {
                blockCachedService.countBlocks()
            } else {
                if(blockNumberRange.first > 0) {
                    blockNumberRange.last - blockNumberRange.first
                } else {
                    blockNumberRange.last + 1
                }
            }
        PageUtils.checkPageParameter(simplePageRequest, count)

        val entityIds =
            if(blockNumberRange == null) {
                blockCachedService.getBlockNumbers(simplePageRequest)
            } else {
                blockCachedService.getBlocks(blockNumberRange.first, blockNumberRange.last, simplePageRequest)
            }
        val contents = entityIds.map { it.number }.run { blockCachedService.getBlocks(this) }
        return PageUtils.getPage(contents, simplePageRequest, count)
    }

    fun getBlocks(numbers: List<Long>) = blockCachedService.getBlocks(numbers)

    fun getBlock(number: Long) = blockCachedService.getBlock(number)

    fun getBlocksByProposer(
        proposer: String,
        blockNumberRange: LongRange? = null,
        simplePageRequest: SimplePageRequest
    ): Page<Block> {
        val count =
            if(blockNumberRange == null) {
                blockCachedService.countBlocksByProposer(proposer)
            } else {
                blockCachedService.countBlocksByProposerAndBlockNumberBetween(
                    proposer, blockNumberRange.first, blockNumberRange.last)
            }
        PageUtils.checkPageParameter(simplePageRequest, count)

        val entityIds =
            if(blockNumberRange == null) {
                blockCachedService.getBlockNumbersByProposer(proposer, simplePageRequest)
            } else {
                blockCachedService.getBlockNumbersByProposerAndBlockNumberBetween(
                    proposer, blockNumberRange.first, blockNumberRange.last, simplePageRequest)
            }
        val contents = entityIds.map { it.number }.run { blockCachedService.getBlocks(this) }
        return PageUtils.getPage(contents, simplePageRequest, count)
    }

    fun <T> getBlocksByProposerAndDateAndNumber(
        type: Class<T>,
        proposer: String,
        date: String,
        number: Long,
        limit: Int,
    ): List<T> {
        val simplePageRequest = SimplePageRequest(1, limit)
        return blockCachedService.getBlocksByProposerAndDateAndNumber(type, proposer, date, number, simplePageRequest)
    }

    fun getLatestNumber(withCache: Boolean = true) =
        if(withCache) {
            blockCachedService.getLatestNumber()
        } else {
            blockCachedService.getLatestNumberWithoutCache()
        }

    fun getBlockBurn(blockNumber: Long) = blockBurnCachedService.getBlockBurn(blockNumber)

    fun getNumberByTimestamp(timestamp: Int) = blockCachedService.getNumberByTimestamp(timestamp)?.number

    fun getTotalTransactionCountByTimestamp(startTimestamp: Long, endTimestamp: Long) =
        blockCachedService.getTransactionCountByTimestamp(startTimestamp, endTimestamp) ?: 0L
}

@Service
class BlockCachedService(
    private val blockRepository: BlockRepository,
    private val cacheUtils: CacheUtils,
    private val finderServerPaging: FinderServerPaging
) {
    private val blockSort = Sort.by(Sort.Order.desc("number"))

    fun getBlock(number: Long): Block? {
        val blocks = getBlocks(listOf(number))
        return if (blocks.size == 1) {
            blocks[0]
        } else {
            null
        }
    }

    fun getBlocks(searchNumbers: List<Long>): List<Block> {
        val blockMap =
            cacheUtils.getEntities(
                CacheName.BLOCK_BY_NUMBER,
                Block::class.java,
                Block::number,
                searchNumbers,
                blockRepository::findAllByNumberIn)

        return searchNumbers.filter { blockMap.containsKey(it) }
            .mapNotNull { blockMap[it] }.toList()
    }

    fun getBlocks(blockNumberStart: Long, blockNumberEnd: Long, simplePageRequest: SimplePageRequest) =
        blockRepository.findAllByNumberBetween(
            blockNumberStart, blockNumberEnd, simplePageRequest.pageRequest(blockSort))

    fun getBlockNumbersByProposer(proposer: String, simplePageRequest: SimplePageRequest) =
        blockRepository.findAllByProposer(proposer, simplePageRequest.pageRequest())

    fun getBlockNumbersByProposerAndBlockNumberBetween(
        proposer: String, blockNumberStart: Long, blockNumberEnd: Long, simplePageRequest: SimplePageRequest) =
        blockRepository.findAllByProposerAndBlockNumberBetween(
            proposer, blockNumberStart, blockNumberEnd, simplePageRequest.pageRequest())

    fun getBlockNumbers(simplePageRequest: SimplePageRequest) =
        blockRepository.findAllBy(simplePageRequest.pageRequest(blockSort))

    fun <T> getBlocksByProposerAndDateAndNumber(
        type: Class<T>,
        proposer: String,
        date: String,
        number: Long,
        simplePageRequest: SimplePageRequest,
    ): List<T> =
        blockRepository.findAllByProposerAndDateAndNumberLessThan(
            type, proposer, date, number, simplePageRequest.pageRequest(blockSort))

    fun countBlocks() =
        blockRepository.countAll(finderServerPaging.limit.block)

    fun countBlocksByProposer(proposer: String) =
        blockRepository.countAllByProposer(proposer, finderServerPaging.limit.block)

    fun countBlocksByProposerAndBlockNumberBetween(proposer: String, blockNumberStart: Long, blockNumberEnd: Long) =
        blockRepository.countAllByProposerAndBlockNumberBetween(
            proposer, blockNumberStart, blockNumberEnd, finderServerPaging.limit.block)

    @Cacheable(cacheNames = [CacheName.BLOCK_LATEST_NUMBER])
    fun getLatestNumber() = getLatestNumberWithoutCache()

    fun getLatestNumberWithoutCache() = blockRepository.findLatestNumber() ?: 0L

    fun getNumberByTimestamp(timestamp: Int) =
        blockRepository.findTop1ByTimestampLessThanEqualOrderByTimestampDescNumberDesc(timestamp)

    @Cacheable(cacheNames = [CacheName.STAT_TOTAL_TRANSACTION_COUNT],
        key="{#timestampStart, #timestampEnd}", unless = "#result == null")
    fun getTransactionCountByTimestamp(timestampStart: Long, timestampEnd: Long) =
        blockRepository.findTotalTransactionCountTimestampBetween(timestampStart, timestampEnd)
}

@Service
class BlockBurnCachedService(
    private val blockBurnRepository: BlockBurnRepository,
) {
    @Cacheable(cacheNames = [CacheName.BLOCK_BURN], key = "#blockNumber", unless = "#result == null")
    fun getBlockBurn(blockNumber: Long) =
        blockBurnRepository.findFirstByNumberLessThanEqual(blockNumber)
}