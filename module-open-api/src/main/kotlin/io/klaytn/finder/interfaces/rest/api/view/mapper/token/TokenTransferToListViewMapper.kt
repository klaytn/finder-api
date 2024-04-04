package io.klaytn.finder.interfaces.rest.api.view.mapper.token

import io.klaytn.commons.model.mapper.ListMapper
import io.klaytn.finder.domain.mysql.set3.token.TokenTransfer
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.infra.utils.applyDecimal
import io.klaytn.finder.interfaces.rest.api.view.model.ContractSummary
import io.klaytn.finder.interfaces.rest.api.view.model.token.TokenTransferListView
import io.klaytn.finder.service.ContractService
import io.klaytn.finder.service.TransactionService
import org.springframework.stereotype.Component

@Component
class TokenTransferToListViewMapper(
    private val contractService: ContractService,
    private val transactionService: TransactionService
) : ListMapper<TokenTransfer, TokenTransferListView> {
    override fun transform(source: List<TokenTransfer>): List<TokenTransferListView> {
        val contractMap = contractService.getContractMap(source.map { it.contractAddress }.toSet())
        val transactionMap = transactionService.getTransactionByHashes(source.map { it.transactionHash }).associateBy { it.transactionHash }

        return source.map { tokenTransfer ->
            val contract = contractMap[tokenTransfer.contractAddress]
            val transaction = transactionMap[tokenTransfer.transactionHash]
            val contractSummary = ContractSummary.of(tokenTransfer.contractAddress, contract)
            val amount = contract?.let { tokenTransfer.amount.toBigDecimal().applyDecimal(it.decimal) }
                ?: tokenTransfer.amount.toBigDecimal()

            TokenTransferListView(
                contract = contractSummary,
                blockId = tokenTransfer.blockNumber,
                transactionHash = tokenTransfer.transactionHash,
                transactionIndex = transaction?.transactionIndex,
                datetime = DateUtils.from(tokenTransfer.timestamp),
                from = tokenTransfer.from.address,
                to = tokenTransfer.to?.address,
                amount = amount
            )
        }
    }
}