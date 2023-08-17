package io.klaytn.finder.interfaces.rest.api.view.mapper.nft

import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.interfaces.rest.api.view.model.ContractSummary
import io.klaytn.finder.interfaces.rest.api.view.model.nft.NftTokenItemView
import io.klaytn.finder.service.ContractService
import io.klaytn.finder.service.nft.NftInventoryRefreshRequestService
import io.klaytn.finder.service.nft.NftTokenItem
import org.springframework.stereotype.Component

@Component
class NftTokenItemToViewMapper(
    private val contractService: ContractService,
    private val nftInventoryRefreshRequestService: NftInventoryRefreshRequestService,
) : Mapper<NftTokenItem, NftTokenItemView> {
    override fun transform(source: NftTokenItem): NftTokenItemView {
        val contract = contractService.getContract(source.contractAddress)
        val tokenUriRefreshable = !nftInventoryRefreshRequestService.existsRefreshNftTokenUriLimiter(
            source.contractAddress, source.tokenId)

        return NftTokenItemView(
            contract = ContractSummary.of(source.contractAddress, contract),
            tokenId = source.tokenId,
            tokenUri = source.tokenUri,
            holder = source.holderAddress?.address,
            totalSupply = source.totalSupply,
            totalTransfer = source.totalTransfer,
            tokenUriRefreshable = tokenUriRefreshable
        )
    }
}