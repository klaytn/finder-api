package io.klaytn.finder.service

import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.domain.common.WalletType
import io.klaytn.finder.domain.mysql.set1.Contract
import io.klaytn.finder.domain.mysql.set1.ContractRepository
import io.klaytn.finder.infra.cache.CacheName
import io.klaytn.finder.infra.cache.CacheUtils
import io.klaytn.finder.infra.client.opensearch.ContractSearchClient
import io.klaytn.finder.infra.client.opensearch.model.ContractSearchRequest
import io.klaytn.finder.infra.db.DbConstants
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.service.caver.CaverAccountService
import org.apache.commons.collections4.map.CaseInsensitiveMap
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class ContractService(
    private val contractCachedService: ContractCachedService,
    private val contractRepository: ContractRepository,
    private val contractSearchClient: ContractSearchClient,
    private val accountService: AccountService,
    private val caverAccountService: CaverAccountService
) {
    fun getContract(address: String) = contractCachedService.getContract(address)

    fun getContracts(addresses: List<String>) = contractCachedService.getContracts(addresses.distinct())

    fun getContractMap(addresses: Set<String>) =
        if (addresses.isNotEmpty()) {
            contractCachedService.getContractMap(addresses)
        } else {
            emptyMap()
        }

    fun search(
        contractSearchRequest: ContractSearchRequest
    ): Page<Contract> {
        val searchIdsPage = contractSearchClient.searchIds(contractSearchRequest)
        val contents = searchIdsPage.content.run { contractCachedService.getContracts(this) }
        return PageImpl(contents, contractSearchRequest.contractSearchPageRequest.pageRequest(), searchIdsPage.totalElements)
    }

    fun getContractsByContractDeployerAddress(
        contractDeployerAddress: String,
        contractTypes: Set<ContractType>,
        simplePageRequest: SimplePageRequest
    ): Page<Contract> {
        val accountAddressPage = accountService.getAccountByContractDeployerAddress(
            contractDeployerAddress, contractTypes, simplePageRequest)

        val contracts = contractCachedService.getContracts(accountAddressPage.content.map { it.address })
        return PageImpl(contracts, simplePageRequest.pageRequest(), accountAddressPage.totalElements)
    }

    @Transactional(DbConstants.set1TransactionManager)
    fun saveContract(contract: Contract): Contract {
        return contractRepository.save(contract).also {
            registerAfterCommitSynchronization(it)
        }
    }

    /**
     * Returns only if there is a single proxy with the contractAddress.
     * - In the case of more than 2, it's difficult to determine which one is the proxy.
     */
    fun getProxyAddressByImplementationAddress(contractAddress: String): String? {
        val proxiesAddress = contractRepository.findFirst2ByImplementationAddress(contractAddress)
        return if(proxiesAddress.size == 1) {
            proxiesAddress[0].contractAddress
        } else {
            null
        }
    }

    /**
     * Determines if the contractAddress is an implementationContract.
     */
    fun isImplementationContract(contractAddress: String) =
        contractRepository.existsByImplementationAddress(contractAddress)

    /**
     * Returns a list of proxies using the contractAddress.
     */
    fun getContractsByImplementationAddress(
        contractAddress: String,
        simplePageRequest: SimplePageRequest
    ): Page<Contract> {
        val contractAddressPage = contractRepository.findAllByImplementationAddress(
            contractAddress, simplePageRequest.pageRequest(Sort.by(Sort.Order.desc("id"))))
        val contracts = contractCachedService.getContracts(contractAddressPage.content.map { it.contractAddress })
        return PageImpl(contracts, simplePageRequest.pageRequest(), contractAddressPage.totalElements)
    }


    /**
     * Uses the ContractCreatorSignature information to determine the owner information of the contractAddress.
     */
    fun verifyContractOwner(
        contractAddress: String,
        walletType: WalletType,
        contractCreatorSignature: String
    ): Boolean {
        val account = accountService.getAccount(contractAddress)
        val contractDeployerAddress = account.contractDeployerAddress

        val signMessage = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + contractAddress
        val recoveredAccountAddressFor = caverAccountService.getRecoveredAccountAddress(
            walletType, signMessage, contractCreatorSignature)
        return recoveredAccountAddressFor.equals(contractDeployerAddress, true)
    }

    private fun registerAfterCommitSynchronization(contract: Contract) {
        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    contractCachedService.flush(contract.contractAddress)
                    contractSearchClient.updateContract(contract)
                }
            })
    }
}

@Service
class ContractCachedService(
    private val contractRepository: ContractRepository,
    private val cacheUtils: CacheUtils,
) {
    fun getContract(address: String): Contract? {
        val contracts = getContracts(listOf(address))

        return if (contracts.size == 1) {
            contracts[0]
        } else {
            null
        }
    }

    fun getContractMap(searchAddresses: Set<String>) =
        CaseInsensitiveMap(
            cacheUtils.getEntities(CacheName.CONTRACT_BY_ADDRESS,
                Contract::class.java,
                Contract::contractAddress,
                searchAddresses,
                contractRepository::findAllByContractAddressIn)
        )

    fun getContracts(searchAddresses: List<String>): List<Contract> {
        val entityMap = getContractMap(searchAddresses.toSet())
        return searchAddresses.filter { entityMap.containsKey(it) }.mapNotNull { entityMap[it] }.toList()
    }

    @CacheEvict(cacheNames = [CacheName.CONTRACT_BY_ADDRESS], key = "#contractAddress")
    fun flush(contractAddress: String) {
    }
}