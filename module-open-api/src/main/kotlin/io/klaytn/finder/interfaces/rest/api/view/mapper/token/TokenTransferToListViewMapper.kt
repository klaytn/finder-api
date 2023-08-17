package io.klaytn.finder.interfaces.rest.api.view.mapper.token

import io.klaytn.commons.model.mapper.ListMapper
import io.klaytn.finder.domain.mysql.set3.token.TokenTransfer
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.infra.utils.applyDecimal
import io.klaytn.finder.interfaces.rest.api.view.model.ContractSummary
import io.klaytn.finder.interfaces.rest.api.view.model.token.TokenTransferListView
import io.klaytn.finder.service.ContractService
import org.springframework.stereotype.Component

@Component
class TokenTransferToListViewMapper(
    private val contractService: ContractService,
) : ListMapper<TokenTransfer, TokenTransferListView> {
    override fun transform(source: List<TokenTransfer>): List<TokenTransferListView> {
        val contractMap = contractService.getContractMap(source.map { it.contractAddress }.toSet())

        return source.map { tokenTransfer ->
            val contract = contractMap[tokenTransfer.contractAddress]
            val contractSummary = ContractSummary.of(tokenTransfer.contractAddress, contract)
            val amount = contract?.let { tokenTransfer.amount.toBigDecimal().applyDecimal(it.decimal) }
                ?: tokenTransfer.amount.toBigDecimal()

            TokenTransferListView(
                contract = contractSummary,
                blockId = tokenTransfer.blockNumber,
                transactionHash = tokenTransfer.transactionHash,
                datetime = DateUtils.from(tokenTransfer.timestamp),
                from = tokenTransfer.from.address,
                to = tokenTransfer.to?.address,
                amount = amount
            )
        }
    }
}