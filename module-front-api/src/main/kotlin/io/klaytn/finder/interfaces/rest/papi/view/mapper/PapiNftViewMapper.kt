package io.klaytn.finder.interfaces.rest.papi.view.mapper

import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.domain.mysql.set3.nft.NftInventory
import io.klaytn.finder.interfaces.rest.api.view.model.contract.ContractSummary
import io.klaytn.finder.interfaces.rest.papi.view.PaiNftInventoryListView
import io.klaytn.finder.service.ContractService
import org.springframework.stereotype.Component

@Component
class PapiNftInventoryToListViewMapper(
    private val contractService: ContractService,
) : Mapper<NftInventory, PaiNftInventoryListView> {
    override fun transform(source: NftInventory): PaiNftInventoryListView {
        val contract = contractService.getContract(source.contractAddress)
        return PaiNftInventoryListView(
            nft = ContractSummary.of(source.contractAddress, contract),
            nftType = source.contractType,
            tokenId = source.tokenId,
            tokenUri = source.tokenUri,
            tokenCount = source.tokenCount
        )
    }
}