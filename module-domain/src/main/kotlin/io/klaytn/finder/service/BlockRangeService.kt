package io.klaytn.finder.service

import io.klaytn.finder.config.dynamic.FinderServerPagingIntervalConfig
import io.klaytn.finder.config.dynamic.FinderServerPangingBlockRangeActiveConfig
import io.klaytn.finder.infra.exception.InvalidRequestException
import io.klaytn.finder.infra.web.model.BlockRangeRequest
import org.springframework.stereotype.Service

@Service
class BlockRangeService(
    private val blockService: BlockService,
    private val finderServerPagingIntervalConfig: FinderServerPagingIntervalConfig,
    private val finderServerPangingBlockRangeActiveConfig: FinderServerPangingBlockRangeActiveConfig,
) {
    fun getBlockRange(blockRangeRequest: BlockRangeRequest?, type: BlockRangeIntervalType): LongRange? {
        if(blockRangeRequest?.toLongRange() == null && !active(type)) {
            return null
        }

        val blockNumberStart = blockRangeRequest?.blockNumberStart
        val blockNumberEnd = blockRangeRequest?.blockNumberEnd
        val latestNumber = blockService.getLatestNumber(false)

        val blockInterval = type.getPagingInterval(finderServerPagingIntervalConfig)
        if (blockNumberStart == null && blockNumberEnd == null) {
            val blockStart = latestNumber - blockInterval
            return LongRange(blockStart.coerceAtLeast(0), latestNumber)
        } else if (blockNumberStart != null && blockNumberEnd == null) {
            val blockStart = blockNumberStart.coerceAtMost(latestNumber)
            val blockEnd = blockStart + blockInterval
            return LongRange(blockStart, blockEnd.coerceAtMost(latestNumber))
        } else if (blockNumberStart == null && blockNumberEnd != null) {
            val blockEnd = blockNumberEnd.coerceAtMost(latestNumber)
            val blockStart = blockEnd - blockInterval
            return LongRange(blockStart.coerceAtLeast(0), blockEnd)
        }

        val blockStart = blockNumberStart!!.coerceAtMost(latestNumber)
        val blockEnd = blockNumberEnd!!.coerceAtMost(latestNumber)
        if(blockStart > blockEnd) {
            throw InvalidRequestException("check range of block. (start number must be before end)")
        }
        if(blockEnd - blockStart > blockInterval) {
            throw InvalidRequestException("check range of block. (max interval over)")
        }
        return LongRange(blockStart, blockEnd)
    }

    fun getBlockRangeCondition(blockRange: LongRange?, type: BlockRangeIntervalType) =
        blockRange?.let {
            mapOf(
                "blockNumberStart" to it.first,
                "blockNumberEnd" to it.last,
                "blockInterval" to type.getPagingInterval(finderServerPagingIntervalConfig))
        }

    private fun active(blockRangeIntervalType: BlockRangeIntervalType) =
        with(finderServerPangingBlockRangeActiveConfig) {
            when(blockRangeIntervalType) {
                BlockRangeIntervalType.BLOCK -> block
                BlockRangeIntervalType.TRANSACTION -> transaction
                BlockRangeIntervalType.INTERNAL_TRANSACTION -> internalTransaction
                BlockRangeIntervalType.NFT_TRANSFER -> nftTransfer
                BlockRangeIntervalType.TOKEN_TRANSFER -> tokenTransfer
                BlockRangeIntervalType.TOKEN_BURN -> tokenBurn
                BlockRangeIntervalType.EVENT_LOG -> eventLog
            }
        }
}

enum class BlockRangeIntervalType {
    BLOCK {
        override fun getPagingInterval(finderServerPagingIntervalConfig: FinderServerPagingIntervalConfig) =
            finderServerPagingIntervalConfig.block
    },
    TRANSACTION {
        override fun getPagingInterval(finderServerPagingIntervalConfig: FinderServerPagingIntervalConfig) =
            finderServerPagingIntervalConfig.transaction
    },
    INTERNAL_TRANSACTION {
        override fun getPagingInterval(finderServerPagingIntervalConfig: FinderServerPagingIntervalConfig) =
            finderServerPagingIntervalConfig.internalTransaction
    },
    NFT_TRANSFER {
        override fun getPagingInterval(finderServerPagingIntervalConfig: FinderServerPagingIntervalConfig) =
            finderServerPagingIntervalConfig.nftTransfer
    },
    TOKEN_TRANSFER {
        override fun getPagingInterval(finderServerPagingIntervalConfig: FinderServerPagingIntervalConfig) =
            finderServerPagingIntervalConfig.tokenTransfer
    },
    TOKEN_BURN {
        override fun getPagingInterval(finderServerPagingIntervalConfig: FinderServerPagingIntervalConfig) =
            finderServerPagingIntervalConfig.tokenBurn
    },
    EVENT_LOG {
        override fun getPagingInterval(finderServerPagingIntervalConfig: FinderServerPagingIntervalConfig) =
            finderServerPagingIntervalConfig.eventLog
    }
    ;

    abstract fun getPagingInterval(finderServerPagingIntervalConfig: FinderServerPagingIntervalConfig): Long;
}