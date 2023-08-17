package io.klaytn.finder.interfaces.rest.api.view.mapper

import io.klaytn.commons.model.mapper.ListMapper
import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.domain.mysql.set1.Block
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.interfaces.rest.api.view.model.block.*
import io.klaytn.finder.service.AccountService
import io.klaytn.finder.service.BlockRewardDelegator
import io.klaytn.finder.service.caver.CaverBlockService
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class BlockToBlockListViewMapper(
    private val blockRewardDelegator: BlockRewardDelegator,
    private val accountService: AccountService,
) : ListMapper<Block, BlockListView> {
    override fun transform(source: List<Block>): List<BlockListView> {
        val blockProposers = source.map { it.proposer }.toSet()
        val accountMap = accountService.getAccountMap(blockProposers)
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
                blockProposerLabel = accountMap[block.proposer]?.addressLabel,
                rewardKlay = reward,
                blockSize = block.size,
                baseFeePerGas = block.baseFeePerGas ?: BigDecimal.ZERO,
                burntFees = burntFees
            )
        }
    }
}

@Component
class BlockToBlockItemViewMapper(
    private val caverBlockService: CaverBlockService,
    private val blockRewardDelegator: BlockRewardDelegator,
    private val accountService: AccountService,
) : Mapper<Block, BlockItemView> {
    override fun transform(source: Block): BlockItemView {
        val proposer = source.proposer
        val validators = caverBlockService.getCommittee(source.number).filterNotNull().filter { it != proposer }.toList()
        val blockReward = blockRewardDelegator.getRewardItem(source)

        val accountAddresses = mutableListOf<String>()
        accountAddresses.addAll(validators)
        accountAddresses.add(proposer)
        val accountMap = accountService.getAccountMap(accountAddresses.toSet())

        val proposerInfo = BlockCommitteeAddressView(proposer, accountMap[proposer]?.addressLabel)
        val validatorInfos = validators.map { BlockCommitteeAddressView(it, accountMap[it]?.addressLabel) }

        return BlockItemView(
            blockId = source.number,
            datetime = DateUtils.from(source.timestamp),
            hash = source.hash,
            parentHash = source.parentHash,
            totalTransactionCount = source.transactionCount,
            blockReward = BlockRewardView(
                mintedKlay = blockReward.minted,
                totalFee = blockReward.totalFee,
                burntFee = blockReward.burntFee
            ),
            blockSize = source.size,
            blockCommittee = BlockCommitteeView(proposerInfo, validatorInfos),
            baseFeePerGas = source.baseFeePerGas ?: BigDecimal.ZERO,
            burntFees = blockReward.burntFee,
        )
    }
}