package io.klaytn.finder.interfaces.rest.api.view.mapper

import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.domain.mysql.set1.Block
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.interfaces.rest.api.view.model.block.BlockCommitteeView
import io.klaytn.finder.interfaces.rest.api.view.model.block.BlockRewardView
import io.klaytn.finder.interfaces.rest.api.view.model.block.BlockView
import io.klaytn.finder.service.BlockRewardDelegator
import io.klaytn.finder.service.caver.CaverBlockService
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class BlockToViewMapper(
    private val caverBlockService: CaverBlockService,
    private val blockRewardDelegator: BlockRewardDelegator,
) : Mapper<Block, BlockView> {
    override fun transform(source: Block): BlockView {
        val proposer = source.proposer
        val validators = caverBlockService.getCommittee(source.number).filterNotNull().filter { it != proposer }.toList()
        val blockReward = blockRewardDelegator.getRewardItem(source)

        return BlockView(
            blockId = source.number,
            datetime = DateUtils.from(source.timestamp),
            hash = source.hash,
            parentHash = source.parentHash,
            totalTransactionCount = source.transactionCount,
            blockReward = BlockRewardView(
                minted = blockReward.minted,
                totalFee = blockReward.totalFee,
                burntFee = blockReward.burntFee
            ),
            blockSize = source.size,
            blockCommittee = BlockCommitteeView(proposer, validators),
            baseFeePerGas = source.baseFeePerGas ?: BigDecimal.ZERO,
        )
    }
}