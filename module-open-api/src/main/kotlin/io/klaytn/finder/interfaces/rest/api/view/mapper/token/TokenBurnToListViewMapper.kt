package io.klaytn.finder.interfaces.rest.api.view.mapper.token

import io.klaytn.commons.model.mapper.ListMapper
import io.klaytn.finder.domain.mysql.set3.token.TokenBurn
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.infra.utils.applyDecimal
import io.klaytn.finder.interfaces.rest.api.view.model.token.TokenBurnListView
import io.klaytn.finder.service.ContractService
import org.springframework.stereotype.Component

@Component
class TokenBurnToListViewMapper(
    private val contractService: ContractService,
) : ListMapper<TokenBurn, TokenBurnListView> {
    override fun transform(source: List<TokenBurn>): List<TokenBurnListView> {
        val contractMap = contractService.getContractMap(source.map { it.contractAddress }.toSet())
        return source.map { tokenBurn ->
            val contract = contractMap[tokenBurn.contractAddress]
            val amount = contract?.let { tokenBurn.amount.toBigDecimal().applyDecimal(it.decimal) }
                ?: tokenBurn.amount.toBigDecimal()

            TokenBurnListView(
                blockId = tokenBurn.blockNumber,
                transactionHash = tokenBurn.transactionHash,
                datetime = DateUtils.from(tokenBurn.timestamp),
                from = tokenBurn.from.address,
                to = tokenBurn.to?.address,
                amount = amount
            )
        }
    }
}