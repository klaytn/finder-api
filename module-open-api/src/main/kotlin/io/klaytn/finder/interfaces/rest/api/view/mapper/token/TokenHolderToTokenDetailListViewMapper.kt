package io.klaytn.finder.interfaces.rest.api.view.mapper.token

import io.klaytn.commons.model.mapper.ListMapper
import io.klaytn.finder.domain.mysql.set3.token.TokenHolder
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.infra.utils.applyDecimal
import io.klaytn.finder.interfaces.rest.api.view.model.ContractDetail
import io.klaytn.finder.interfaces.rest.api.view.model.token.TokenDetailListView
import io.klaytn.finder.service.ContractService
import org.springframework.stereotype.Component

@Component
class TokenHolderToTokenDetailListViewMapper(
    private val contractService: ContractService,
) : ListMapper<TokenHolder, TokenDetailListView> {
    override fun transform(source: List<TokenHolder>): List<TokenDetailListView> {
        val contractMap = contractService.getContractMap(source.map { it.contractAddress }.toSet())

        return source.map { tokenHolder ->
            val contract = contractMap[tokenHolder.contractAddress]
            val amount = contract?.let { tokenHolder.amount.toBigDecimal().applyDecimal(it.decimal) }
                ?: tokenHolder.amount.toBigDecimal()

            TokenDetailListView(
                contract = ContractDetail.of(tokenHolder.contractAddress, contract),
                balance = amount,
                latestTransactionDateTime = DateUtils.from(tokenHolder.lastTransactionTime)
            )
        }
    }
}