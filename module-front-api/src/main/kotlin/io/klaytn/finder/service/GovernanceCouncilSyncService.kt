package io.klaytn.finder.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.klaytn.commons.utils.logback.logger
import io.klaytn.commons.utils.retrofit2.orElseThrow
import io.klaytn.finder.config.ClientProperties
import io.klaytn.finder.domain.common.GovernanceCouncilContractType
import io.klaytn.finder.domain.mysql.set1.governancecouncil.GovernanceCouncil
import io.klaytn.finder.domain.mysql.set1.governancecouncil.GovernanceCouncilContract
import io.klaytn.finder.domain.mysql.set1.governancecouncil.GovernanceCouncilContractRepository
import io.klaytn.finder.domain.mysql.set1.governancecouncil.GovernanceCouncilRepository
import io.klaytn.finder.infra.client.KlaytnSquareClient
import io.klaytn.finder.infra.db.DbConstants
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.service.governancecouncil.GovernanceCouncilCachedService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@Service
class GovernanceCouncilSyncService(
    private val governanceCouncilCachedService: GovernanceCouncilCachedService,
    private val accountUpdateService: AccountUpdateService,
    private val klaytnSquareClient: KlaytnSquareClient,
    private val clientProperties: ClientProperties,
    private val objectMapper: ObjectMapper,
    private val governanceCouncilRepository: GovernanceCouncilRepository,
    private val governanceCouncilContractRepository: GovernanceCouncilContractRepository,
) {
    private final val logger = logger(this::class.java)
    private final val squareWebUrl = clientProperties.urls["square-web"]!!

    @Transactional(DbConstants.set1TransactionManager)
    fun doSync(dryRun: Boolean): Boolean {
        // from Klatn-Square
        val governanceCouncilsFromAPI = klaytnSquareClient.getGovernanceCouncils().orElseThrow { IllegalStateException(it) }
        val governanceCouncilMapFromAPI =
            governanceCouncilsFromAPI.result.map {
                klaytnSquareClient.getGovernanceCouncil(it.id).orElseThrow { message -> IllegalStateException(message) }
            }.associate { it.result.id to it.result }

        if(governanceCouncilsFromAPI.result.isEmpty() || governanceCouncilMapFromAPI.isEmpty()) {
            logger.warn("[illegal state] gc-count:${governanceCouncilsFromAPI.result.size}, gc-contracts-size:${governanceCouncilMapFromAPI.size}")
            return false
        }

        // from DB
        val governanceCouncilMapFromDB = governanceCouncilRepository.findAll().associateBy { it.squareId }
        val governanceCouncilContractMapFromDB =
            governanceCouncilContractRepository.findAll().associateBy { it.address }

        // Checklist of items to be deleted from the database
        val deletedSquareIds = governanceCouncilMapFromDB.keys.toMutableSet()
        val deletedSquareContractAddresses = governanceCouncilContractMapFromDB.keys.toMutableSet()

        val newGovernanceCouncils = mutableListOf<GovernanceCouncil>()
        val newGovernanceCouncilContracts = mutableListOf<GovernanceCouncilContract>()

        val deleteGovernanceCouncils = mutableListOf<GovernanceCouncil>()
        val deleteGovernanceCouncilContracts = mutableListOf<GovernanceCouncilContract>()

        governanceCouncilsFromAPI.result.forEach {
            val squareId = it.id
            val squareDetailWebUrl = "$squareWebUrl/GC/Detail?id=${it.id}"
            var gcWebsites: List<String> = emptyList()
            val websitesList = it.websites as? List<Map<String, String>>
            if (websitesList != null) {
                gcWebsites = websitesList.mapNotNull { it["url"] }.filter { site -> site.isNotBlank() }
            }


            val newGovernanceCouncil =
                governanceCouncilMapFromDB.getOrDefault(squareId,
                    GovernanceCouncil.of(
                        squareId = squareId,
                        squareLink = squareDetailWebUrl,
                        name = it.name,
                        thumbnail = it.thumbnail,
                        website = gcWebsites,
                        joinedAt = DateUtils.dateToLocalDateTime(it.joinedAt)))
            with(newGovernanceCouncil) {
                if(this.id != 0L) {
                    this.squareLink = squareDetailWebUrl
                    this.name = it.name
                    this.thumbnail = it.thumbnail
                    this.website = gcWebsites
                    this.joinedAt = DateUtils.dateToLocalDateTime(it.joinedAt)
                }
                newGovernanceCouncils.add(this)
            }

            governanceCouncilMapFromAPI[squareId]?.let { gcDetailResult ->
                gcDetailResult.contracts.forEach{ gcContract ->
                    if(!governanceCouncilContractMapFromDB.containsKey(gcContract.address)) {
                        newGovernanceCouncilContracts.add(
                            GovernanceCouncilContract(
                                squareId = squareId,
                                address = gcContract.address,
                                addressType = GovernanceCouncilContractType.of(gcContract.type)))
                    }
                    deletedSquareContractAddresses.remove(gcContract.address)
                }
            }

            deletedSquareIds.remove(squareId)
        }

        deletedSquareIds.forEach { deactivatedSquareId ->
            governanceCouncilMapFromDB[deactivatedSquareId]?.let { gc ->
                deleteGovernanceCouncils.add(gc)
            }
        }

        deletedSquareContractAddresses.forEach {
            governanceCouncilContractMapFromDB[it]?.let { gcContract ->
                deleteGovernanceCouncilContracts.add(gcContract)
            }
        }

        if(!dryRun) {
            if(newGovernanceCouncils.isNotEmpty()) {
                governanceCouncilRepository.saveAll(newGovernanceCouncils)
            }
            if(deleteGovernanceCouncils.isNotEmpty()) {
                governanceCouncilRepository.deleteAll(deleteGovernanceCouncils)
            }

            if(newGovernanceCouncilContracts.isNotEmpty()) {
                governanceCouncilContractRepository.saveAll(newGovernanceCouncilContracts)
            }
            if(deleteGovernanceCouncilContracts.isNotEmpty()) {
                governanceCouncilContractRepository.deleteAll(deleteGovernanceCouncilContracts)
            }

            val allSquareIds = mutableSetOf<Long>()
            governanceCouncilMapFromAPI.keys.map { allSquareIds.add(it) }
            governanceCouncilMapFromDB.keys.map { allSquareIds.add(it) }

            registerAfterCommitSynchronization(
                allSquareIds,
                newGovernanceCouncilContracts,
                deleteGovernanceCouncilContracts)
        }

        return true
    }

    private fun registerAfterCommitSynchronization(
        allSquareIds: Set<Long>,
        newGovernanceCouncilContracts: List<GovernanceCouncilContract>,
        deleteGovernanceCouncilContracts: List<GovernanceCouncilContract>,
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