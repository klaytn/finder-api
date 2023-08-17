package io.klaytn.finder.service

import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.domain.mysql.set1.Account
import io.klaytn.finder.domain.mysql.set1.AccountAddressOnly
import io.klaytn.finder.domain.mysql.set1.AccountRepository
import io.klaytn.finder.infra.cache.CacheName
import io.klaytn.finder.infra.cache.CacheUtils
import io.klaytn.finder.infra.client.opensearch.AccountSearchClient
import io.klaytn.finder.infra.client.opensearch.AccountSearchType
import io.klaytn.finder.infra.db.DbConstants
import io.klaytn.finder.infra.exception.InvalidRequestException
import io.klaytn.finder.infra.exception.NotFoundAccountException
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.service.caver.CaverAccountService
import io.klaytn.finder.service.caver.CaverKnsService
import org.apache.commons.collections4.map.CaseInsensitiveMap
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.math.BigDecimal

@Service
class AccountService(
    private val accountCachedService: AccountCachedService,
    private val accountRepository: AccountRepository,
    private val caverAccountService: CaverAccountService,
    private val caverKnsService: CaverKnsService,
    private val accountSearchClient: AccountSearchClient,
) {
    /**
     * An accountAddress is a string that starts with 0x or ends with .klay.
     */
    fun checkAndGetAddress(accountAddress: String) =
        if(accountAddress.endsWith(".klay")) {
            caverKnsService.getAddress(accountAddress) ?: throw NotFoundAccountException()
        } else if(caverAccountService.isAccountAddress(accountAddress)) {
            accountAddress
        } else {
            throw InvalidRequestException()
        }

    /**
     * If not present in the database, replace and return with the value obtained through en.
     */
    fun getAccount(address: String): Account {
        val account = accountCachedService.getAccount(address)
        if(account != null) {
            return account
        }

        val accountType = caverAccountService.getAccountType(address)
        val transactionCount = caverAccountService.getTransactionCount(address)
        return Account.of(
            address = address,
            accountType = accountType,
            balance = BigDecimal.ZERO,                         // Correction in the mapper.
            totalTransactionCount = transactionCount.toLong(),
            contractType = ContractType.CUSTOM,
        )
    }

    fun getAccountMap(addresses: Set<String>) =
        if (addresses.isNotEmpty()) {
            accountCachedService.getAccountMap(addresses)
        } else {
            emptyMap()
        }

    fun search(accountSearchType: AccountSearchType, keyword: String, simplePageRequest: SimplePageRequest): Page<Account> {
        val searchedIdPage = accountSearchClient.searchIds(accountSearchType, keyword, simplePageRequest)
        val accounts = accountCachedService.getAccounts(searchedIdPage.content)
        return PageImpl(accounts, simplePageRequest.pageRequest(), searchedIdPage.totalElements)
    }

    fun getAccountByContractDeployerAddress(
        contractDeployerAddress: String,
        contractTypes: Set<ContractType>,
        simplePageRequest: SimplePageRequest
    ): Page<AccountAddressOnly> =
        accountRepository.findAllByContractDeployerAddressAndContractTypeIn(
            contractDeployerAddress,
            contractTypes,
            simplePageRequest.pageRequest(Sort.by(Sort.Order.desc("createdAt")))
        )

    @Transactional(DbConstants.set1TransactionManager)
    fun updateAddressLabel(accountAddress: String, addressLabel: String?) =
        accountRepository.findAllByAddressIn(listOf(accountAddress)).firstOrNull()?.let {
            it.addressLabel = addressLabel
            accountRepository.save(it)
        }.also {
            registerAfterCommitSynchronization(accountAddress)
        }

    @Transactional(DbConstants.set1TransactionManager)
    fun updateKnsDomain(accountAddress: String, knsDomain: String?) =
        accountRepository.findAllByAddressIn(listOf(accountAddress)).firstOrNull()?.let {
            it.knsDomain = knsDomain
            accountRepository.save(it)
        }.also {
            registerAfterCommitSynchronization(accountAddress)
        }

    @Transactional(DbConstants.set1TransactionManager)
    fun updateTags(accountAddress: String, tags: List<String>) =
        accountRepository.findAllByAddressIn(listOf(accountAddress)).firstOrNull()?.let { account ->
            account.tags = tags
            accountRepository.save(account)
        }.also {
            registerAfterCommitSynchronization(accountAddress)
        }

    @Transactional(DbConstants.set1TransactionManager)
    fun addTags(accountAddress: String, newTags: List<String>) =
        accountRepository.findAllByAddressIn(listOf(accountAddress)).firstOrNull()?.let { account ->
            val tags = account.tags?.toMutableList() ?: mutableListOf()
            tags.addAll(newTags.filterNot { tags.contains(it) })

            account.tags = tags
            accountRepository.save(account)
        }.also {
            registerAfterCommitSynchronization(accountAddress)
        }

    @Transactional(DbConstants.set1TransactionManager)
    fun removeTags(accountAddress: String, removeTags: List<String>) =
        accountRepository.findAllByAddressIn(listOf(accountAddress)).firstOrNull()?.let { account ->
            account.tags = account.tags?.filterNot { removeTags.contains(it) }
            accountRepository.save(account)
        }.also {
            registerAfterCommitSynchronization(accountAddress)
        }

    private fun registerAfterCommitSynchronization(accountAddress: String) {
        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    accountCachedService.flush(accountAddress)
                }
            })
    }
}

@Service
class AccountCachedService(
    private val accountRepository: AccountRepository,
    private val cacheUtils: CacheUtils,
) {
    fun getAccount(address: String): Account? {
        val accounts = getAccountMap(setOf(address))

        return if (accounts.size == 1) {
            accounts[address]
        } else {
            null
        }
    }

    fun getAccountMap(searchAddresses: Collection<String>) =
        CaseInsensitiveMap(
            cacheUtils.getEntities(CacheName.ACCOUNT_BY_ADDRESS,
                Account::class.java,
                Account::address,
                searchAddresses,
                accountRepository::findAllByAddressIn)
        )

    fun getAccounts(searchAddresses: List<String>): List<Account> {
        val entityMap = getAccountMap(searchAddresses.toSet())
        return searchAddresses.filter { entityMap.containsKey(it) }.mapNotNull { entityMap[it] }.toList()
    }

    @CacheEvict(cacheNames = [CacheName.ACCOUNT_BY_ADDRESS], key = "#accountAddress")
    fun flush(accountAddress: String) {
    }
}