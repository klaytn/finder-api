package io.klaytn.finder.interfaces.rest.api.view.mapper.nft

import io.klaytn.commons.model.mapper.ListMapper
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.interfaces.rest.api.view.model.ContractSummary
import io.klaytn.finder.interfaces.rest.api.view.model.account.AccountNftBalanceListView
import io.klaytn.finder.service.ContractService
import io.klaytn.finder.service.nft.NftHolder
import org.springframework.stereotype.Component

@Component
class NftHolderToAccountNftBalanceListViewMapper(
    private val contractService: ContractService,
) : ListMapper<NftHolder, AccountNftBalanceListView> {
    override fun transform(source: List<NftHolder>): List<AccountNftBalanceListView> {
        val contractMap = contractService.getContractMap(source.map { it.contractAddress }.toSet())

        return source.map { nftHolder ->
            val contract = contractMap[nftHolder.contractAddress]
            AccountNftBalanceListView(
                contract = ContractSummary.of(nftHolder.contractAddress, contract),
                tokenId = nftHolder.tokenId,
                tokenCount = nftHolder.tokenCount,
                latestTransaction = DateUtils.from(nftHolder.lastTransactionTime)
            )
        }
    }
}