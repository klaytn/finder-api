package io.klaytn.finder.service.governancecouncil

import io.klaytn.finder.domain.mysql.set1.governancecouncil.GovernanceCouncilContract
import io.klaytn.finder.domain.mysql.set1.governancecouncil.GovernanceCouncilContractRepository
import io.klaytn.finder.domain.mysql.set1.governancecouncil.GovernanceCouncilRepository
import io.klaytn.finder.infra.cache.CacheName
import io.klaytn.finder.infra.cache.CacheUtils
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class GovernanceCouncilService(
    val governanceCouncilCachedService: GovernanceCouncilCachedService,
) {
    fun getGovernanceCouncil(address: String) =
        governanceCouncilCachedService.getGovernanceCouncilContractIdByAddress(address)?.let {
            governanceCouncilCachedService.getGovernanceCouncilContractById(it)?.let { gcContract ->
                governanceCouncilCachedService.getGovernanceCouncil(gcContract.squareId)
            }
        }

    fun getGovernanceCouncilContracts(squareId: Long) =
        governanceCouncilCachedService.getGovernanceCouncilContractIdsBySquareId(squareId)
            .run {
                governanceCouncilCachedService.getGovernanceCouncilContracts(this)
            }
}

@Service
class GovernanceCouncilCachedService(
    private val governanceCouncilRepository: GovernanceCouncilRepository,
    private val governanceCouncilContractRepository: GovernanceCouncilContractRepository,
    private val cacheUtils: CacheUtils,
) {
    @Cacheable(cacheNames = [CacheName.GOVERNANCE_COUNCIL_BY_SQUARE_ID], key = "#squareId", unless = "#result == null")
    fun getGovernanceCouncil(squareId: Long) =
        governanceCouncilRepository.findBySquareId(squareId)

    @Cacheable(
        cacheNames = [CacheName.GOVERNANCE_COUNCIL_CONTRACT_ID_BY_ADDRESS],
        key = "#address",
        unless = "#result == null"
    )
    fun getGovernanceCouncilContractIdByAddress(address: String) =
        governanceCouncilContractRepository.findByAddress(address)?.id

    @Cacheable(
        cacheNames = [CacheName.GOVERNANCE_COUNCIL_CONTRACT_IDS_BY_SQUARE_ID],
        key = "#squareId",
        unless = "#result == null"
    )
    fun getGovernanceCouncilContractIdsBySquareId(squareId: Long) =
        governanceCouncilContractRepository.findAllBySquareId(squareId).map { it.id }

    fun getGovernanceCouncilContractById(id: Long): GovernanceCouncilContract? {
        val contracts = getGovernanceCouncilContracts(listOf(id))
        return if (contracts.isNotEmpty()) contracts[0] else null
    }

    fun getGovernanceCouncilContracts(searchIds: List<Long>) =
        cacheUtils.getEntities(
            CacheName.GOVERNANCE_COUNCIL_CONTRACT,
            GovernanceCouncilContract::class.java,
            searchIds,
            governanceCouncilContractRepository
        )

    @CacheEvict(
        cacheNames = [
            CacheName.GOVERNANCE_COUNCIL_BY_SQUARE_ID,
            CacheName.GOVERNANCE_COUNCIL_CONTRACT_IDS_BY_SQUARE_ID],
        key = "#squareId")
    fun flushBySquareId(squareId: Long) {
    }
}
