package io.klaytn.finder.view.mapper

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.config.ChainProperties
import io.klaytn.finder.domain.mysql.set1.BlockReward
import io.klaytn.finder.infra.utils.KlayUtils
import io.klaytn.finder.service.BlockService
import io.klaytn.finder.view.model.BlockRewardDistribution
import io.klaytn.finder.view.model.BlockRewardTarget
import io.klaytn.finder.view.model.BlockRewardView
import io.klaytn.finder.view.model.BlockRewordAddressType
import org.springframework.stereotype.Component

@Component
class BlockRewardToViewMapper(
    private val chainProperties: ChainProperties,
    private val objectMapper: ObjectMapper,
    private val blockService: BlockService,
): Mapper<BlockReward, BlockRewardView?> {
    override fun transform(source: BlockReward): BlockRewardView? {
        val block = blockService.getBlock(source.number) ?: return null

        val minted = KlayUtils.pebToKlay(source.minted.toString())
        val totalFee = KlayUtils.pebToKlay(source.totalFee.toString())
        val burntFee = KlayUtils.pebToKlay(source.burntFee.toString())
        val proposer = KlayUtils.pebToKlay(source.proposer.toString())
        val stakers = KlayUtils.pebToKlay(source.stakers.toString())
        val kgf = KlayUtils.pebToKlay(source.kgf.toString())
        val kir = KlayUtils.pebToKlay(source.kir.toString())

        val rewardMap = objectMapper.readValue(source.rewards.toByteArray(),
            object : TypeReference<Map<String, String>>() {})

        val addressRewordMap = rewardMap.map {
            it.key to KlayUtils.pebToKlay(it.value.substring(2).toBigInteger(16).toString())
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
                    if(address.equals(block.reward, true)) {
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


        // https://baobab.klaytnfinder.io/block/111936138?tabId=rewardDetails
        // As some stakers are transitioning to proposers,
        // when (proposer+stakers+kgf+kir) exceeds (minted+totalFee-burntFee),
        // the stakers need to be adjusted using the following calculations.
//        val revisedStakers = stakers - ((proposer+stakers+kgf+kir) - (minted+totalFee-burntFee))
//        val proposerStaker =
//            revisedStakers - recipients.filter { it.type == BlockRewordAddressType.STAKER }.sumOf { it.amount }
//        val proposerReminder =
//            recipients.first { it.type == BlockRewordAddressType.PROPOSER }.amount - proposer - proposerStaker



        return BlockRewardView(
            minted = minted,
            totalFee = totalFee,
            burntFee = burntFee,
            distributions = distributions,
            recipients = recipients,
        )
    }
}

