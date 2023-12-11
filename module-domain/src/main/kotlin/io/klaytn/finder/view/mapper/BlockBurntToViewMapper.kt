package io.klaytn.finder.view.mapper

import io.klaytn.finder.domain.mysql.set1.BlockBurn
import io.klaytn.finder.infra.utils.KlayUtils
import io.klaytn.finder.view.model.BlockBurnView
import org.springframework.stereotype.Component
import java.math.BigDecimal

// FIXME: Refactor this mapper to use the same pattern as other mappers.
interface IBlockBurntToViewMapper {
    fun transform(source: BlockBurn, kip103Burnt: BigDecimal): BlockBurnView
}

@Component
class BlockBurntToViewMapper: IBlockBurntToViewMapper {
    override fun transform(source: BlockBurn, kip103Burnt: BigDecimal): BlockBurnView {
        return BlockBurnView(
            nearestBlockNumber = source.number,
            accumulateBurntFees = KlayUtils.pebToKlay(source.accumulateFees.toString()),
            accumulateBurntKlay = KlayUtils.pebToKlay(source.accumulateKlay.toString()),
            kip103Burnt = kip103Burnt
        )
    }
}