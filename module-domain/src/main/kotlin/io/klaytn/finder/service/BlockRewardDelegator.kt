package io.klaytn.finder.service

import io.klaytn.finder.config.ChainProperties
import io.klaytn.finder.domain.mysql.set1.Block
import io.klaytn.finder.domain.mysql.set1.BlockReward
import io.klaytn.finder.infra.utils.KlayUtils
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class BlockRewardDelegator(
    private val gasPriceService: GasPriceService,
    private val chainProperties: ChainProperties,
    private val blockRewardService: BlockRewardService
) {
    private val defaultMintValue = BigDecimal(9.6)

    fun getBlockReward(numbers: List<Long>) =
        blockRewardService.getBlockRewards(numbers)

    fun getRewardItem(block: Block): BlockRewardItem {
        val blockReward = blockRewardService.getBlockReward(block.number)
        return getRewardItem(block, blockReward)
    }

    fun getRewardItems(blocks: List<Block>): Map<Long,BlockRewardItem> {
        val blockMap = blocks.associateBy { it.number }
        val blockRewardMap = blockRewardService.getBlockRewards(blocks.map { it.number })

        return blockMap.entries.associate {
            it.key to getRewardItem(it.value, blockRewardMap[it.key]) }
    }

    fun getRewardItem(block: Block, blockReward: BlockReward?): BlockRewardItem {
        val minted = blockReward?.let { KlayUtils.pebToKlay(it.minted.toString()) }
            ?: getMintValue(block.number)
        val totalFee = blockReward?.let { KlayUtils.pebToKlay(it.totalFee.toString()) }
            ?: getTransactionFee(block.number, block.baseFeePerGas, block.gasUsed)
        val burntFee = blockReward?.let { KlayUtils.pebToKlay(it.burntFee.toString()) }
            ?: getBurntFeeBelowMagnaVersion(block.number, totalFee)
        return BlockRewardItem(
            number = block.number,
            minted = minted,
            totalFee = totalFee,
            burntFee = burntFee ?: BigDecimal.ZERO
        )
    }

    fun getRewardItem(blockNumber:Long, baseFeePerGas: BigDecimal?, gasUsed: Int,
                      blockReward: BlockReward?): BlockRewardItem {
        val minted = blockReward?.let { KlayUtils.pebToKlay(it.minted.toString()) }
            ?: getMintValue(blockNumber)
        val totalFee = blockReward?.let { KlayUtils.pebToKlay(it.totalFee.toString()) }
            ?: getTransactionFee(blockNumber, baseFeePerGas, gasUsed)
        val burntFee = blockReward?.let { KlayUtils.pebToKlay(it.burntFee.toString()) }
            ?: getBurntFeeBelowMagnaVersion(blockNumber, totalFee)
        return BlockRewardItem(
            number = blockNumber,
            minted = minted,
            totalFee = totalFee,
            burntFee = burntFee ?: BigDecimal.ZERO
        )
    }

    /**
     * Total transaction fees for the block.
     */
    private fun getTransactionFee(blockNumber: Long, baseFeePerGas: BigDecimal?, gasUsed: Int): BigDecimal {
        val gasPrice = gasPriceService.getGasPrice(blockNumber, baseFeePerGas)
        return gasUsed.toBigDecimal() * gasPrice
    }

    /**
     * Block minting.
     */
    private fun getMintValue(blockNumber: Long): BigDecimal {
        if(chainProperties.blockMintProperties.isEmpty()) {
            return defaultMintValue
        }

        return chainProperties.blockMintProperties
            .sortedByDescending { it.startBlockNumber }
            .firstOrNull() { blockNumber >= it.startBlockNumber }?.mintValue ?: defaultMintValue
    }

    /**
     * Rewards up to the MAGMA version.
     */
    fun getRewardBelowMagnaVersion(blockNumber: Long, baseFeePerGas: BigDecimal?, gasUsed: Int): BigDecimal {
        val minted = getMintValue(blockNumber)
        val totalFee = getTransactionFee(blockNumber, baseFeePerGas, gasUsed)
        val burnt = getBurntFeeBelowMagnaVersion(blockNumber, totalFee)
        return minted.plus(totalFee).minus(burnt)
    }

    /**
     * Block burning amount up to the MAGMA version.
     */
    private fun getBurntFeeBelowMagnaVersion(blockNumber: Long, totalFee: BigDecimal) =
        if(chainProperties.isDynamicFeeTarget(blockNumber)) {
            totalFee.divide(BigDecimal(2))
        } else {
            BigDecimal.ZERO
        }
}

data class BlockRewardItem(
    val number: Long,
    val minted: BigDecimal,
    val totalFee: BigDecimal,
    val burntFee: BigDecimal
) {
    fun totalReward() = minted.plus(totalFee).minus(burntFee)
}