package io.klaytn.finder.service

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.klaytn.finder.domain.mysql.set1.AccountTag
import io.klaytn.finder.domain.mysql.set1.AccountTagRepository
import io.klaytn.finder.infra.cache.CacheName
import io.klaytn.finder.infra.cache.CacheValue
import io.klaytn.finder.infra.db.DbConstants
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.util.concurrent.TimeUnit

@Service
class AccountTagService(
    private val accountTagRepository: AccountTagRepository,
    private val accountTagCachedService: AccountTagCachedService,
) {
    private var accountTagCache: Cache<String, List<String>> = Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .maximumSize(10)
        .build()

    fun getAll(): List<String> {
        val managedTags = accountTagCache.getIfPresent(CacheValue.ALL)
        if(!managedTags.isNullOrEmpty()) {
            return managedTags
        }

        return accountTagCachedService.getAllAccountTags()
            .filter { accountTag -> accountTag.display }
            .sortedBy { it.tagOrder }
            .map { it.tag }
            .toList()
            .also { accountTagCache.put(CacheValue.ALL, it) }
    }

    fun getSortedTags(tags: List<String>?) =
        if (!tags.isNullOrEmpty()) {
            val managedTags = getAll()
            if(managedTags.isNullOrEmpty() || managedTags.contains("*")) {
                tags
            } else {
                managedTags.filter { tags.contains(it) }.toList()
            }
        } else {
            null
        }

    @Transactional(DbConstants.set1TransactionManager)
    fun updateAccountTag(accountTag: AccountTag): AccountTag {
        val searchedAccountTag = accountTagRepository.findAccountTagByTag(accountTag.tag)?.also {
            it.tagOrder = accountTag.tagOrder
            it.display = accountTag.display
        } ?: accountTag

        return accountTagRepository.save(searchedAccountTag).also {
            registerAfterCommitSynchronization()
        }
    }

    @Transactional(DbConstants.set1TransactionManager)
    fun deleteAccountTag(tag: String): Long {
        return accountTagRepository.deleteAccountTagByTag(tag).also {
            registerAfterCommitSynchronization()
        }
    }

    private fun registerAfterCommitSynchronization() {
        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    accountTagCachedService.flush()
                    accountTagCache.invalidate(CacheValue.ALL)
                }
            })
    }
}

@Service
class AccountTagCachedService(
    private val accountTagRepository: AccountTagRepository
) {
    @Cacheable(cacheNames = [CacheName.ACCOUNT_TAGS], key = CacheValue.ALL, unless = "#result == null")
    fun getAllAccountTags(): List<AccountTag> = accountTagRepository.findAll()

    @CacheEvict(cacheNames = [CacheName.ACCOUNT_TAGS], key = CacheValue.ALL)
    fun flush() {
    }
}