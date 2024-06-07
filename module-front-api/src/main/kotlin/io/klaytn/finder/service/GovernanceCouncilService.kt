package io.klaytn.finder.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.klaytn.commons.utils.logback.logger
import io.klaytn.commons.utils.retrofit2.orElseThrow
import io.klaytn.finder.config.ClientProperties
import io.klaytn.finder.domain.common.GovernanceCouncilContractType
import io.klaytn.finder.domain.mysql.set4.GovernanceCouncilsInfo
import io.klaytn.finder.domain.mysql.set4.GovernanceCouncilCategories
import io.klaytn.finder.domain.mysql.set4.GovernanceCouncilCommunities
import io.klaytn.finder.domain.mysql.set4.GovernanceCouncilContracts
import io.klaytn.finder.domain.mysql.set4.GovernanceCouncilsInfoRepository
import io.klaytn.finder.domain.mysql.set4.GovernanceCouncilCategoriesRepository
import io.klaytn.finder.domain.mysql.set4.GovernanceCouncilCommunitiesRepository
import io.klaytn.finder.domain.mysql.set4.GovernanceCouncilContractsRepository
import io.klaytn.finder.infra.client.KaiaSquareClient
import io.klaytn.finder.infra.client.KlaytnSquareClient
import io.klaytn.finder.infra.db.DbConstants
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.service.governancecouncil.GovernanceCouncilCachedService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@Service
class GovernanceCouncilInfoService(
    private val governanceCouncilCachedService: GovernanceCouncilCachedService,
    private val accountUpdateService: AccountUpdateService,
    private val kaiaSquareClient: KaiaSquareClient,
    private val clientProperties: ClientProperties,
    private val objectMapper: ObjectMapper,
    private val governanceCouncilsInfoRepository: GovernanceCouncilsInfoRepository,
    private val governanceCouncilCategoriesRepository: GovernanceCouncilCategoriesRepository,
    private val governanceCouncilCommunitiesRepository: GovernanceCouncilCommunitiesRepository,
    private val governanceCouncilContractsRepository: GovernanceCouncilContractsRepository,
) {
    private final val logger = logger(this::class.java)
    private final val squareWebUrl = clientProperties.urls["square-web"]!!

    @Transactional(DbConstants.set4TransactionManager)
    fun doSync(dryRun: Boolean): Any {
        // from Klaytn-Square
        val governanceCouncilsFromAPI = kaiaSquareClient.getGovernanceCouncils().orElseThrow { IllegalStateException(it) }

        return governanceCouncilsFromAPI
    }

}