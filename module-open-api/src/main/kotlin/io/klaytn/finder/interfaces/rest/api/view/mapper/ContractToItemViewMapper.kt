package io.klaytn.finder.interfaces.rest.api.view.mapper

import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.domain.mysql.set1.Contract
import io.klaytn.finder.interfaces.rest.api.view.ContractItemView
import org.springframework.stereotype.Component

@Component
class ContractToItemViewMapper(
) : Mapper<Contract, ContractItemView> {
    override fun transform(source: Contract): ContractItemView {
        return ContractItemView(
            address = source.contractAddress,
            type = source.contractType,
            name = source.name,
            symbol = source.symbol,
            decimal = source.decimal,
            totalSupply = source.totalSupply,
            icon = source.icon,
            officialSite = source.officialSite,
            officialEmailAddress = source.officialEmailAddress,
            verified = source.verified,
        )
    }
}