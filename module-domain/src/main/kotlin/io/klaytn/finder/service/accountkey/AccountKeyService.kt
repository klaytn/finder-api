package io.klaytn.finder.service.accountkey

import io.klaytn.finder.domain.mysql.set1.*
import io.klaytn.finder.infra.cache.CacheName
import io.klaytn.finder.infra.cache.CacheUtils
import io.klaytn.finder.infra.utils.PageUtils
import io.klaytn.finder.infra.web.model.SimplePageRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class AccountKeyService(
    val accountKeyCachedService: AccountKeyCachedService,
) {
    fun getAccountKeysByAccountAddress(accountAddress: String, simplePageRequest: SimplePageRequest): Page<AccountKey> {
        val page = accountKeyCachedService.getAccountKeyIds(accountAddress, simplePageRequest)
        val accountKeys = accountKeyCachedService.getAccountKeys(page.content.map { it.id })
        return PageUtils.getPage(accountKeys, simplePageRequest, page.totalElements)
    }

    fun getAccountKeyByTransactionHash(transactionHash: String): AccountKey? {
        val accountKeyId = accountKeyCachedService.getAccountKeyIdByTransactionHash(transactionHash)
        return accountKeyId?.let { accountKeyCachedService.getAccountKey(it.id) }
    }
}

@Service
class AccountKeyCachedService(
    private val accountKeyRepository: AccountKeyRepository,
    private val cacheUtils: CacheUtils,
) {
    private val blockNumberSort = Sort.by(Sort.Order.desc("blockNumber"))

    fun getAccountKeyIds(accountAddress: String, simplePageRequest: SimplePageRequest) =
        accountKeyRepository.findAllByAccountAddress(accountAddress, simplePageRequest.pageRequest(blockNumberSort))

    fun getAccountKeyIdByTransactionHash(transactionHash: String) =
        accountKeyRepository.findByTransactionHash(transactionHash)

    fun getAccountKey(id: Long): AccountKey? {
        val accountKeys = getAccountKeys(listOf(id))

        return if (accountKeys.size == 1) {
            accountKeys[0]
        } else {
            null
        }
    }

    fun getAccountKeys(searchIds: List<Long>) =
        cacheUtils.getEntities(
            CacheName.ACCOUNT_KEY_BY_ID,
            AccountKey::class.java,
            searchIds,
            accountKeyRepository)
}
