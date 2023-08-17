package io.klaytn.finder.service

import io.klaytn.finder.domain.mysql.set1.approve.AccountNftApprove
import io.klaytn.finder.domain.mysql.set1.approve.AccountNftApproveRepository
import io.klaytn.finder.domain.mysql.set1.approve.AccountTokenApprove
import io.klaytn.finder.domain.mysql.set1.approve.AccountTokenApproveRepository
import io.klaytn.finder.infra.cache.CacheName
import io.klaytn.finder.infra.cache.CacheUtils
import io.klaytn.finder.infra.web.model.SimplePageRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class AccountApproveService(
    private val accountTokenApproveRepository: AccountTokenApproveRepository,
    private val accountNftApproveRepository: AccountNftApproveRepository,
    private val accountTokenApproveCachedService: AccountTokenApproveCachedService,
    private val accountNftApproveCachedService: AccountNftApproveCachedService
) {
    private val accountApproveSort = Sort.by(Sort.Order.desc("blockNumber"))

    fun getApporvedTokens(
        accountAddress: String,
        spenderAddress: String?,
        simplePageRequest: SimplePageRequest
    ): Page<AccountTokenApprove> {
        val pageable = simplePageRequest.pageRequest(accountApproveSort)
        val searchedIdPage =
            if(spenderAddress.isNullOrBlank()) {
                accountTokenApproveRepository.findAllByAccountAddress(accountAddress, pageable)
            } else {
                accountTokenApproveRepository.findAllByAccountAddressAndSpenderAddress(
                    accountAddress, spenderAddress, pageable)
            }

        val tokenApproves = accountTokenApproveCachedService.getAccountTokenApproves(
            searchedIdPage.content.map { it.id })
        return PageImpl(tokenApproves, simplePageRequest.pageRequest(), searchedIdPage.totalElements)
    }

    fun getApprovedNfts(
        accountAddress: String,
        spenderAddress: String?,
        approvedAll: Boolean,
        simplePageRequest: SimplePageRequest
    ): Page<AccountNftApprove> {
        val pageable = simplePageRequest.pageRequest(accountApproveSort)
        val searchedIdPage =
            if(spenderAddress.isNullOrBlank()) {
                accountNftApproveRepository.findAllByAccountAddressAndApprovedAll(
                    accountAddress, approvedAll, pageable)
            } else {
                accountNftApproveRepository.findAllByAccountAddressAndSpenderAddressAndApprovedAll(
                    accountAddress, spenderAddress, approvedAll, pageable)
            }

        val nftApproves = accountNftApproveCachedService.getAccountNftApproves(
            searchedIdPage.content.map { it.id })
        return PageImpl(nftApproves, simplePageRequest.pageRequest(), searchedIdPage.totalElements)
    }
}

@Service
class AccountTokenApproveCachedService(
    private val accountAddressService: AccountAddressService,
    private val accountTokenApproveRepository: AccountTokenApproveRepository,
    private val cacheUtils: CacheUtils,
) {
    fun getAccountTokenApproves(searchIds: List<Long>): List<AccountTokenApprove> {
        val accountTokenApproves =
            cacheUtils.getEntities(
                CacheName.ACCOUNT_TOKEN_APPROVE,
                AccountTokenApprove::class.java,
                searchIds,
                accountTokenApproveRepository
            )

        val accountAddresses = accountTokenApproves.map { it.spenderAddress }.toList()
        accountAddressService.fillAccountAddress(accountAddresses)
        return accountTokenApproves
    }
}

@Service
class AccountNftApproveCachedService(
    private val accountAddressService: AccountAddressService,
    private val accountNftApproveRepository: AccountNftApproveRepository,
    private val cacheUtils: CacheUtils,
) {
    fun getAccountNftApproves(searchIds: List<Long>): List<AccountNftApprove> {
        val accountNftApproves =
            cacheUtils.getEntities(
                CacheName.ACCOUNT_NFT_APPROVE,
                AccountNftApprove::class.java,
                searchIds,
                accountNftApproveRepository
            )

        val accountAddresses = accountNftApproves.map { it.spenderAddress }.toList()
        accountAddressService.fillAccountAddress(accountAddresses)
        return accountNftApproves
    }
}