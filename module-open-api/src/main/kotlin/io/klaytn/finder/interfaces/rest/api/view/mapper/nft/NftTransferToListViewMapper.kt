package io.klaytn.finder.interfaces.rest.api.view.mapper.nft

import io.klaytn.commons.model.mapper.ListMapper
import io.klaytn.finder.domain.mysql.set3.nft.NftTransfer
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.interfaces.rest.api.view.model.ContractSummary
import io.klaytn.finder.interfaces.rest.api.view.model.nft.NftTransferListView
import io.klaytn.finder.service.ContractService
import org.springframework.stereotype.Component

@Component
class NftTransferToListViewMapper(
    private val contractService: ContractService,
) : ListMapper<NftTransfer, NftTransferListView> {
    override fun transform(source: List<NftTransfer>): List<NftTransferListView> {
        val contractMap = contractService.getContractMap(source.map { it.contractAddress }.toSet())

        return source.map { nftTransfer ->
            val contract = contractMap[nftTransfer.contractAddress]
            NftTransferListView(
                contract = ContractSummary.of(nftTransfer.contractAddress, contract),
                blockId = nftTransfer.blockNumber,
                transactionHash = nftTransfer.transactionHash,
                datetime = DateUtils.from(nftTransfer.timestamp),
                from = nftTransfer.from.address,
                to = nftTransfer.to?.address,
                tokenId = nftTransfer.tokenId,
                tokenCount = nftTransfer.tokenCount
            )
        }
    }
}