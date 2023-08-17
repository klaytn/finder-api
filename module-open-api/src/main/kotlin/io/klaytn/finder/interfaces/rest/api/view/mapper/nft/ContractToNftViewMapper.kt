package io.klaytn.finder.interfaces.rest.api.view.mapper.nft

import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.domain.mysql.set1.Contract
import io.klaytn.finder.infra.utils.applyDecimal
import io.klaytn.finder.interfaces.rest.api.view.model.nft.NftView
import io.klaytn.finder.service.caver.CaverContractService
import org.springframework.stereotype.Component

@Component
class ContractToNftViewMapper(
    private val caverContractService: CaverContractService,
) : Mapper<Contract, NftView> {
    override fun transform(source: Contract): NftView {
        val caverTotalSupply = caverContractService.getTotalSupply(source.contractType, source.contractAddress)
        val totalSupply = caverTotalSupply?.toBigDecimal() ?: source.totalSupply

        return NftView(
            contractType = source.contractType,
            name = source.name,
            symbol = source.symbol,
            icon = source.icon,
            totalSupply = totalSupply.applyDecimal(source.decimal),
            totalTransfers = source.totalTransfer,
            officialSite = source.officialSite,
            holderCount = source.holderCount,
        )
    }
}