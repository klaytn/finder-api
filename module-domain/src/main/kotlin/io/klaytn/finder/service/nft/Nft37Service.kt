package io.klaytn.finder.service.nft

import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.infra.utils.PageUtils
import io.klaytn.finder.infra.web.model.SimplePageRequest
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
class Nft37Service(
    private val nftInventoryService: NftInventoryService,
) {
    private val contractType = ContractType.KIP37

    // -- --------------------------------------------------------------------------------------------------------------
    // -- nft balances
    // -- --------------------------------------------------------------------------------------------------------------

    fun getNftBalancesByHolder(
        holderAddress: String,
        simplePageRequest: SimplePageRequest,
    ): Page<NftHolder> {
        val page = nftInventoryService.getNft37InventoriesByHolderAddress(holderAddress, simplePageRequest)
        val contents = page.content
            .map {
                NftHolder(
                    contractType = contractType,
                    contractAddress = it.contractAddress,
                    holderAddress = it.holderAddress,
                    tokenCount = it.tokenCount,
                    tokenId = it.tokenId,
                    tokenUri = it.tokenUri,
                    lastTransactionTime = it.lastTransactionTime
                )
            }
        return PageUtils.getPage(contents, simplePageRequest, page.totalElements)
    }

    // -- --------------------------------------------------------------------------------------------------------------
    // -- nft holders
    // -- --------------------------------------------------------------------------------------------------------------

    fun getNftHolders(
        contractAddress: String,
        tokenId: String?,
        simplePageRequest: SimplePageRequest,
    ): Page<NftHolder> {
        val page = nftInventoryService.getNftInventoriesByContractAddressOrderByTokenCount(
            contractAddress, tokenId, simplePageRequest)
        val contents = page.content
            .map {
                NftHolder(
                    contractType = contractType,
                    contractAddress = it.contractAddress,
                    holderAddress = it.holderAddress,
                    tokenCount = it.tokenCount,
                    tokenId = it.tokenId,
                    tokenUri = it.tokenUri,
                    lastTransactionTime = it.lastTransactionTime
                )
            }
        return PageUtils.getPage(contents, simplePageRequest, page.totalElements)
    }
}
