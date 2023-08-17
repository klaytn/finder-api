package io.klaytn.finder.interfaces.rest.api.view.mapper.nft

import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.domain.mysql.set3.nft.NftInventory
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.interfaces.rest.api.view.model.nft.NftInventoryListView
import org.springframework.stereotype.Component

@Component
class NftInventoryToListViewMapper: Mapper<NftInventory, NftInventoryListView> {
    override fun transform(source: NftInventory): NftInventoryListView {
        return NftInventoryListView(
            tokenId = source.tokenId,
            holder = source.holderAddress.address,
            tokenUri = source.tokenUri,
            tokenCount = source.tokenCount,
            updatedAt = DateUtils.localDateTimeToDate(source.updatedAt!!)
        )
    }
}