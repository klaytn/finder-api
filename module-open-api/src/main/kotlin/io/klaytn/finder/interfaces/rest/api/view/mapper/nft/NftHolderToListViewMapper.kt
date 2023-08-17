package io.klaytn.finder.interfaces.rest.api.view.mapper.nft

import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.infra.utils.applyDecimal
import io.klaytn.finder.interfaces.rest.api.view.model.nft.NftHolderListView
import io.klaytn.finder.service.caver.CaverContractService
import io.klaytn.finder.service.nft.NftHolder
import io.klaytn.finder.service.nft.NftService
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class NftHolderToListViewMapper(
    private val caverContractService: CaverContractService,
    private val nftService: NftService,
) : Mapper<NftHolder, NftHolderListView> {
    override fun transform(source: NftHolder): NftHolderListView {
        val totalSupply =
            if (source.tokenId.isNullOrBlank()) {
                nftService.getNft(source.contractAddress)?.let {
                    it.totalSupply.applyDecimal(it.decimal)
                } ?: BigDecimal.ZERO
            } else {
                caverContractService.getTotalSupplyOfTokenId(source.contractAddress, source.tokenId!!).toBigDecimal()
            }
        val percentage =
            if (totalSupply > BigDecimal.ZERO) {
                BigDecimal(source.tokenCount).multiply(BigDecimal(100)).divide(totalSupply, 4, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

        return NftHolderListView(
            holder = source.holderAddress.address,
            tokenCount = source.tokenCount,
            percentage = percentage,
            tokenId = source.tokenId
        )
    }
}