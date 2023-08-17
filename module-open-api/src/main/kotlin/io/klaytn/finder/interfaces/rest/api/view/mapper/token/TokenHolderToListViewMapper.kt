package io.klaytn.finder.interfaces.rest.api.view.mapper.token

import io.klaytn.commons.model.mapper.ListMapper
import io.klaytn.finder.domain.mysql.set3.token.TokenHolder
import io.klaytn.finder.infra.utils.applyDecimal
import io.klaytn.finder.interfaces.rest.api.view.model.token.TokenHolderListView
import io.klaytn.finder.service.ContractService
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class TokenHolderToListViewMapper(
    private val contractService: ContractService,
) : ListMapper<TokenHolder, TokenHolderListView> {
    override fun transform(source: List<TokenHolder>): List<TokenHolderListView> {
        val contractMap = contractService.getContractMap(source.map { it.contractAddress }.toSet())

        return source.map { tokenHolder ->
            val contract = contractMap[tokenHolder.contractAddress]
            val totalSupply = contract?.let { it.totalSupply.applyDecimal(it.decimal) }
                ?: BigDecimal.ZERO
            val amount = contract?.let { tokenHolder.amount.toBigDecimal().applyDecimal(it.decimal) }
                ?: tokenHolder.amount.toBigDecimal()

            val percentage =
                if (totalSupply > BigDecimal.ZERO) {
                    amount.multiply(BigDecimal(100)).divide(totalSupply, 4, RoundingMode.HALF_UP)
                } else {
                    BigDecimal.ZERO
                }

            TokenHolderListView(
                holder = tokenHolder.holderAddress.address,
                amount = amount,
                percentage = percentage
            )
        }
    }
}