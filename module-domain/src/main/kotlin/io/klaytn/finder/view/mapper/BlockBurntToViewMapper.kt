package io.klaytn.finder.view.mapper

import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.domain.mysql.set1.BlockBurn
import io.klaytn.finder.infra.utils.KlayUtils
import io.klaytn.finder.view.model.BlockBurnView
import org.springframework.stereotype.Component

@Component
class BlockBurntToViewMapper: Mapper<BlockBurn, BlockBurnView> {
    override fun transform(source: BlockBurn): BlockBurnView {
        return BlockBurnView(
            nearestBlockNumber = source.number,
            accumulateBurntFees = KlayUtils.pebToKlay(source.accumulateFees.toString()),
            accumulateBurntKlay = KlayUtils.pebToKlay(source.accumulateKlay.toString()),
        )
    }
}