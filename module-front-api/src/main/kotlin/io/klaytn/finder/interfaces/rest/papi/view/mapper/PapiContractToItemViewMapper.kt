package io.klaytn.finder.interfaces.rest.papi.view.mapper

import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.domain.mysql.set1.Contract
import io.klaytn.finder.interfaces.rest.papi.view.PapiContractItemView
import org.springframework.stereotype.Component

@Component
class PapiContractToItemViewMapper(
) : Mapper<Contract, PapiContractItemView> {
    override fun transform(source: Contract): PapiContractItemView {
        return PapiContractItemView(
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