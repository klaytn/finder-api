package io.klaytn.finder.service

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
import io.klaytn.finder.infra.client.KaiaSquareGovernanceCouncilCommunity
import io.klaytn.finder.infra.client.KaiaSquareGovernanceCouncilCommunityLink
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
    fun doSync(dryRun: Boolean): Boolean {
        // from Klaytn-Square
        val governanceCouncilsFromAPI =
            kaiaSquareClient.getGovernanceCouncils().orElseThrow { IllegalStateException(it) }
        val governanceCouncilMapFromAPI =
            governanceCouncilsFromAPI.result.map {
                kaiaSquareClient.getGovernanceCouncil(it.id).orElseThrow { message -> IllegalStateException(message) }
            }.associate { it.result.id to it.result }

        if (governanceCouncilsFromAPI.result.isEmpty() || governanceCouncilMapFromAPI.isEmpty()) {
            logger.warn("[illegal state] gc-count:${governanceCouncilsFromAPI.result.size}, gc-contracts-size:${governanceCouncilMapFromAPI.size}")
            return false
        }

        // from DB
        val governanceCouncilInfoMapFromDB = governanceCouncilsInfoRepository.findAll().associateBy { it.squareId }
        val governanceCouncilContractMapFromDB =
            governanceCouncilContractsRepository.findAll().associateBy { it.address }
        val governanceCouncilCategoryMapFromDB =
            governanceCouncilCategoriesRepository.findAll().associateBy { Pair(it.squareId, it.categoryId) }
        val governanceCouncilCommunityMapFromDB =
            governanceCouncilCommunitiesRepository.findAll().associateBy { Pair(it.squareId, it.communityId) }

        // Checklist of items to be deleted from the database
        val deletedSquareIds = governanceCouncilInfoMapFromDB.keys.toMutableSet()
        val deletedSquareContractAddresses = governanceCouncilContractMapFromDB.keys.toMutableSet()
        val deletedSquareCategories = governanceCouncilCategoryMapFromDB.keys.toMutableSet()
        val deletedSquareCommunities = governanceCouncilCommunityMapFromDB.keys.toMutableSet()

        val newGovernanceCouncils = mutableListOf<GovernanceCouncilsInfo>()
        val newGovernanceCouncilContracts = mutableListOf<GovernanceCouncilContracts>()
        val newGovernanceCouncilCategories = mutableListOf<GovernanceCouncilCategories>()
        val newGovernanceCouncilCommunities = mutableListOf<GovernanceCouncilCommunities>()

        val deleteGovernanceCouncils = mutableListOf<GovernanceCouncilsInfo>()
        val deleteGovernanceCouncilContracts = mutableListOf<GovernanceCouncilContracts>()
        val deleteGovernanceCouncilCategories = mutableListOf<GovernanceCouncilCategories>()
        val deleteGovernanceCouncilCommunities = mutableListOf<GovernanceCouncilCommunities>()

        governanceCouncilsFromAPI.result.forEach {
            val squareId = it.id
            val squareDetailWebUrl = "$squareWebUrl/GC/Detail?id=${it.id}"
            var gcWebsites: List<String> = emptyList()
            val websitesList = it.websites as? List<Map<String, String>>
            if (websitesList != null) {
                gcWebsites = websitesList.mapNotNull { website -> website["url"] }.filter { site -> site.isNotBlank() }
            }

            val newGovernanceCouncilInfo =
                governanceCouncilInfoMapFromDB.getOrDefault(
                    squareId,
                    GovernanceCouncilsInfo.of(
                        squareId = squareId,
                        squareLink = squareDetailWebUrl,
                        name = it.name,
                        thumbnail = it.thumbnail,
                        website = objectMapper.writeValueAsString(gcWebsites),
                        summary = it.summary,
                        description = it.description,
                        apy = it.staking.apy,
                        totalStaking = it.staking.totalStaking,
                        isFoundation = it.siteConf?.isFoundation ?: false,
                        joinedAt = DateUtils.dateToLocalDateTime(it.joinedAt),
                    )
                )

            with(newGovernanceCouncilInfo) {
                if (this.id != 0L) {
                    this.squareLink = squareDetailWebUrl
                    this.name = it.name
                    this.thumbnail = it.thumbnail
                    this.website = objectMapper.writeValueAsString(gcWebsites)
                    this.summary = it.summary
                    this.description = it.description
                    this.apy = it.staking.apy
                    this.totalStaking = it.staking.totalStaking
                    this.isFoundation = it.siteConf?.isFoundation ?: false
                    this.joinedAt = DateUtils.dateToLocalDateTime(it.joinedAt)
                }
                newGovernanceCouncils.add(this)
            }

            val categories = it.categories
            if (categories.isNotEmpty()) {
                categories.forEach { category ->
                    val categoryId = category.id
                    val categoryName = category.name

                    val newGovernanceCouncilCategory =
                        governanceCouncilCategoryMapFromDB.getOrDefault(
                            Pair(squareId, categoryId),
                            GovernanceCouncilCategories.of(
                                squareId = squareId,
                                categoryId = categoryId,
                                categoryName = categoryName
                            )
                        )

                    with(newGovernanceCouncilCategory) {
                        if (this.id != 0L) {
                            this.categoryName = categoryName
                        }
                        newGovernanceCouncilCategories.add(this)
                    }
                    deletedSquareCategories.remove(Pair(squareId, categoryId))
                }
            }

            governanceCouncilMapFromAPI[squareId]?.let { gcDetailResult ->
                gcDetailResult.contracts.forEach { gcContract ->
                    if (!governanceCouncilContractMapFromDB.containsKey(gcContract.address)) {
                        newGovernanceCouncilContracts.add(
                            GovernanceCouncilContracts(
                                squareId = squareId,
                                address = gcContract.address,
                                addressType = GovernanceCouncilContractType.of(gcContract.type),
                                version = gcContract.version ?: 0
                            )
                        )
                    }
                    deletedSquareContractAddresses.remove(gcContract.address)
                }

                gcDetailResult.communities?.forEach { community ->
                    if (!governanceCouncilCommunityMapFromDB.containsKey(Pair(squareId, community.id))) {
                        val links =
                            (community.links as? List<KaiaSquareGovernanceCouncilCommunityLink>)?.map { link ->
                                link.url
                            }?.filter { site -> site.isNotBlank() } ?: emptyList()

                        newGovernanceCouncilCommunities.add(
                            GovernanceCouncilCommunities(
                                squareId = squareId,
                                communityId = community.id,
                                name = community.name,
                                links = objectMapper.writeValueAsString(
                                    links
                                ),
                                thumbnail = community.thumbnail
                            )
                        )
                    }
                    deletedSquareCommunities.remove(Pair(squareId, community.id))
                }
            }

            deletedSquareIds.remove(squareId)
        }

        deletedSquareIds.forEach { deactivatedSquareId ->
            governanceCouncilInfoMapFromDB[deactivatedSquareId]?.let { gc ->
                deleteGovernanceCouncils.add(gc)
            }
        }

        deletedSquareContractAddresses.forEach {
            governanceCouncilContractMapFromDB[it]?.let { gcContract ->
                deleteGovernanceCouncilContracts.add(gcContract)
            }
        }

        deletedSquareCategories.forEach {
            governanceCouncilCategoryMapFromDB[it]?.let { gcCategory ->
                deleteGovernanceCouncilCategories.add(gcCategory)
            }
        }

        deletedSquareCommunities.forEach {
            governanceCouncilCommunityMapFromDB[it]?.let { gcCommunity ->
                deleteGovernanceCouncilCommunities.add(gcCommunity)
            }
        }

        if (!dryRun) {
            if (newGovernanceCouncils.isNotEmpty()) {
                governanceCouncilsInfoRepository.saveAll(newGovernanceCouncils)
            }
            if (deleteGovernanceCouncils.isNotEmpty()) {
                governanceCouncilsInfoRepository.deleteAll(deleteGovernanceCouncils)
            }

            if (newGovernanceCouncilContracts.isNotEmpty()) {
                governanceCouncilContractsRepository.saveAll(newGovernanceCouncilContracts)
            }
            if (deleteGovernanceCouncilContracts.isNotEmpty()) {
                governanceCouncilContractsRepository.deleteAll(deleteGovernanceCouncilContracts)
            }

            if (newGovernanceCouncilCategories.isNotEmpty()) {
                governanceCouncilCategoriesRepository.saveAll(newGovernanceCouncilCategories)
            }
            if (deleteGovernanceCouncilCategories.isNotEmpty()) {
                governanceCouncilCategoriesRepository.deleteAll(deleteGovernanceCouncilCategories)
            }

            if (newGovernanceCouncilCommunities.isNotEmpty()) {
                governanceCouncilCommunitiesRepository.saveAll(newGovernanceCouncilCommunities)
            }
            if (deleteGovernanceCouncilCommunities.isNotEmpty()) {
                governanceCouncilCommunitiesRepository.deleteAll(deleteGovernanceCouncilCommunities)
            }

            val allSquareIds = mutableSetOf<Long>()
            governanceCouncilMapFromAPI.keys.map { allSquareIds.add(it) }
            governanceCouncilInfoMapFromDB.keys.map { allSquareIds.add(it) }

//            registerAfterCommitSynchronization(
//                allSquareIds,
//                newGovernanceCouncilContracts,
//                deleteGovernanceCouncilContracts
//            )
        }

        return true
    }

    private fun registerAfterCommitSynchronization(
        allSquareIds: Set<Long>,
        newGovernanceCouncilContracts: List<GovernanceCouncilContracts>,
        deleteGovernanceCouncilContracts: List<GovernanceCouncilContracts>,
    ) {
        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    newGovernanceCouncilContracts.forEach { gcContract ->
                        getTagName(gcContract.addressType)?.let {
                            accountUpdateService.addTags(gcContract.address, listOf(it))
                        }
                    }

                    deleteGovernanceCouncilContracts.forEach { gcContract ->
                        getTagName(gcContract.addressType)?.let {
                            accountUpdateService.removeTags(gcContract.address, listOf(it))
                        }
                    }

                    allSquareIds.forEach { squareId ->
                        governanceCouncilCachedService.flushBySquareId(squareId)
                    }
                }
            })
    }

    private fun getTagName(addressType: GovernanceCouncilContractType) =
        when (addressType) {
            GovernanceCouncilContractType.NODE -> "gc_node"
            GovernanceCouncilContractType.STAKING -> "gc_staking"
            GovernanceCouncilContractType.REWARD -> "gc_reward"
            else -> {
                null
            }
        }
}