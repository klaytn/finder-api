package io.klaytn.finder.interfaces.rest.api.view.mapper.token

import io.klaytn.commons.model.mapper.ListMapper
import io.klaytn.finder.domain.mysql.set3.token.TokenHolder
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.infra.utils.applyDecimal
import io.klaytn.finder.interfaces.rest.api.view.model.ContractSummary
import io.klaytn.finder.interfaces.rest.api.view.model.token.TokenBalanceListView
import io.klaytn.finder.service.ContractService
import org.springframework.stereotype.Component

@Component
class TokenHolderToTokenBalanceListViewMapper(
    private val contractService: ContractService,
) : ListMapper<TokenHolder, TokenBalanceListView> {
    override fun transform(source: List<TokenHolder>): List<TokenBalanceListView> {
        val contractMap = contractService.getContractMap(source.map { it.contractAddress }.toSet())

        return source.map { tokenHolder ->
            val contract = contractMap[tokenHolder.contractAddress]
            val amount = contract?.let { tokenHolder.amount.toBigDecimal().applyDecimal(it.decimal) }
                ?: tokenHolder.amount.toBigDecimal()

            TokenBalanceListView(
                contract = ContractSummary.of(tokenHolder.contractAddress, contract),
                balance = amount,
                latestTransactionDateTime = DateUtils.from(tokenHolder.lastTransactionTime)
            )
        }
    }
}