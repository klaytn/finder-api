package io.klaytn.finder.view.mapper

import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.config.ChainProperties
import io.klaytn.finder.domain.mysql.set1.Block
import io.klaytn.finder.infra.utils.KlayUtils
import io.klaytn.finder.service.caver.CaverBlockService
import io.klaytn.finder.view.model.BlockRewardDistribution
import io.klaytn.finder.view.model.BlockRewardTarget
import io.klaytn.finder.view.model.BlockRewardView
import io.klaytn.finder.view.model.BlockRewordAddressType
import org.springframework.stereotype.Component

@Component
class CaverBlockRewardToViewMapper(
    private val caverBlockService: CaverBlockService,
    private val chainProperties: ChainProperties,
): Mapper<Block, BlockRewardView?> {
    override fun transform(source: Block): BlockRewardView? {
        val blockRewards = caverBlockService.getRewards(source.number) ?: return null

        val minted = KlayUtils.pebToKlay(blockRewards.minted)
        val totalFee = KlayUtils.pebToKlay(blockRewards.totalFee)
        val burntFee = KlayUtils.pebToKlay(blockRewards.burntFee)
        val proposer = KlayUtils.pebToKlay(blockRewards.proposer)
        val stakers = KlayUtils.pebToKlay(blockRewards.stakers)
        val kgf = KlayUtils.pebToKlay(blockRewards.kff)
        val kir = KlayUtils.pebToKlay(blockRewards.kcf)
        val addressRewordMap = blockRewards.rewards.map {
            it.key to KlayUtils.pebToKlay(it.value)
        }.toMap()

        val kgfAddress = chainProperties.getManagedAddress(BlockRewordAddressType.KGF.name)
        val kirAddress = chainProperties.getManagedAddress(BlockRewordAddressType.KIR.name)

        val distributions = listOf(
            BlockRewardDistribution(BlockRewordAddressType.PROPOSER, proposer),
            BlockRewardDistribution(BlockRewordAddressType.KGF, kgf),
            BlockRewardDistribution(BlockRewordAddressType.KIR, kir),
            BlockRewardDistribution(BlockRewordAddressType.STAKERS, stakers),
        ).sortedBy { it.type.order }

        val recipients =
            addressRewordMap.map { (address, amount) ->
                val accountAddressType =
                    if(address.equals(source.reward, true)) {
                        BlockRewordAddressType.PROPOSER
                    } else if(address.equals(kgfAddress, true)) {
                        BlockRewordAddressType.KGF
                    } else if(address.equals(kirAddress, true)) {
                        BlockRewordAddressType.KIR
                    } else {
                        BlockRewordAddressType.STAKER
                    }
                BlockRewardTarget(accountAddressType, address, amount)
            }.toList().sortedBy { it.type.order }

        return BlockRewardView(
            minted = minted,
            totalFee = totalFee,
            burntFee = burntFee,
            distributions = distributions,
            recipients = recipients
        )
    }
}

