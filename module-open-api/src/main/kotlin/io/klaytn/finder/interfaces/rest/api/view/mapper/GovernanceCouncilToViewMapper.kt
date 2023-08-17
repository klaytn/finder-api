package io.klaytn.finder.interfaces.rest.api.view.mapper

import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.domain.mysql.set1.governancecouncil.GovernanceCouncil
import io.klaytn.finder.interfaces.rest.api.view.model.governancecouncil.GovernanceCouncilView
import io.klaytn.finder.service.governancecouncil.GovernanceCouncilService
import org.springframework.stereotype.Component

@Component
class GovernanceCouncilToViewMapper(
    private val governanceCouncilService: GovernanceCouncilService,
) : Mapper<GovernanceCouncil, GovernanceCouncilView> {
    override fun transform(source: GovernanceCouncil): GovernanceCouncilView {
        val governanceCouncilContractViews =
            governanceCouncilService.getGovernanceCouncilContracts(source.squareId)
                .groupBy({it.addressType}, {it.address})

        return GovernanceCouncilView(
            squareId = source.squareId,
            name = source.name,
            squareLink = source.squareLink,
            thumbnail = source.thumbnail,
            website = source.website,
            contracts = governanceCouncilContractViews,
            joinedAt = source.joinedAt
        )
    }
}