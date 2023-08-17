package io.klaytn.finder.interfaces.rest.api.view.mapper

import io.klaytn.commons.model.mapper.ListMapper
import io.klaytn.finder.domain.mysql.set1.Block
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.interfaces.rest.api.view.model.block.BlockListView
import io.klaytn.finder.service.BlockRewardDelegator
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class BlockToListViewMapper(
    private val blockRewardDelegator: BlockRewardDelegator,
) : ListMapper<Block, BlockListView> {
    override fun transform(source: List<Block>): List<BlockListView> {
        val blockRewardMap = blockRewardDelegator.getRewardItems(source)

        return source.map { block ->
            val blockReward = blockRewardMap[block.number]!!
            val reward = blockReward.totalReward()
            val burntFees = blockReward.burntFee

            BlockListView(
                blockId = block.number,
                datetime = DateUtils.from(block.timestamp),
                totalTransactionCount = block.transactionCount,
                blockProposer = block.proposer,
                reward = reward,
                blockSize = block.size,
                baseFeePerGas = block.baseFeePerGas ?: BigDecimal.ZERO,
                burntFees = burntFees
            )
        }
    }
}

