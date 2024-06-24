package io.klaytn.finder.interfaces.rest.api.view.mapper

import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.domain.mysql.set1.governancecouncil.GovernanceCouncil
import io.klaytn.finder.domain.mysql.set4.GovernanceCouncilCategories
import io.klaytn.finder.domain.mysql.set4.GovernanceCouncilsInfo
import io.klaytn.finder.interfaces.rest.api.view.model.governancecouncil.GovernanceCouncilCategory
import io.klaytn.finder.interfaces.rest.api.view.model.governancecouncil.GovernanceCouncilView
import io.klaytn.finder.interfaces.rest.api.view.model.governancecouncil.GovernanceCouncilWithCategoryView
import io.klaytn.finder.service.governancecouncil.GovernanceCouncilService
import org.springframework.stereotype.Component
import io.klaytn.commons.utils.logback.logger

@Component
class GovernanceCouncilToViewMapper(
    private val governanceCouncilService: GovernanceCouncilService,
) : Mapper<GovernanceCouncil, GovernanceCouncilView> {
    override fun transform(source: GovernanceCouncil): GovernanceCouncilView {
        val governanceCouncilContractViews =
            governanceCouncilService.getGovernanceCouncilContracts(source.squareId)
                .groupBy({ it.addressType }, { it.address })

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

@Component
class GovernanceCouncilToListViewMapper(
) : Mapper<Pair<List<GovernanceCouncilsInfo>, List<GovernanceCouncilCategories>>, List<GovernanceCouncilWithCategoryView>> {
    private val logger = logger(this::class.java)

    override fun transform(source: Pair<List<GovernanceCouncilsInfo>, List<GovernanceCouncilCategories>>): List<GovernanceCouncilWithCategoryView> {
        val (governanceCouncilInfoList, governanceCouncilCategoriesList) = source

        val governanceCouncilInfoMap = governanceCouncilInfoList.associateBy { it.squareId }
        val governanceCouncilCategoriesMap = governanceCouncilCategoriesList.groupBy { it.squareId }

        return governanceCouncilInfoMap.map { (squareId, governanceCouncilInfo) ->
            val governanceCouncilCategories = this.mapCategories(governanceCouncilCategoriesMap[squareId] ?: emptyList())

            GovernanceCouncilWithCategoryView(
                name = governanceCouncilInfo.name,
                squareLink = governanceCouncilInfo.squareLink,
                thumbnail = governanceCouncilInfo.thumbnail,
                joinedAt = governanceCouncilInfo.joinedAt,
                totalStaking = governanceCouncilInfo.totalStaking,
                apy = governanceCouncilInfo.apy,
                description = governanceCouncilInfo.description,
                categories = governanceCouncilCategories
            )
        }
    }

    private fun mapCategories(categories: List<GovernanceCouncilCategories>): List<GovernanceCouncilCategory> {
        return try {
                 categories.map {
                GovernanceCouncilCategory(it.categoryId, it.categoryName)
                    }
             } catch (e: Exception) {
                logger.error("Error mapping categories", e)
              emptyList()
             }
    }

}