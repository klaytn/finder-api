package io.klaytn.finder.interfaces.rest.api.view.mapper.token

import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.domain.mysql.set1.Contract
import io.klaytn.finder.infra.utils.applyDecimal
import io.klaytn.finder.interfaces.rest.api.view.model.token.TokenView
import io.klaytn.finder.service.caver.CaverContractService
import org.springframework.stereotype.Component

@Component
class ContractToTokenViewMapper(
    private val caverContractService: CaverContractService,
) : Mapper<Contract, TokenView> {
    override fun transform(source: Contract): TokenView {
        val caverTotalSupply = caverContractService.getTotalSupply(source.contractType, source.contractAddress)
        val totalSupply = caverTotalSupply?.toBigDecimal() ?: source.totalSupply
        val burnAmount = source.burnAmount?.toBigDecimal()?.applyDecimal(source.decimal)

        return TokenView(
            contractType = source.contractType,
            name = source.name,
            symbol = source.symbol,
            icon =  source.icon,
            decimal = source.decimal,
            totalSupply = totalSupply.applyDecimal(source.decimal),
            totalTransfers = source.totalTransfer,
            officialSite = source.officialSite,
            burnAmount = burnAmount,
            totalBurns = source.totalBurn
        )
    }
}