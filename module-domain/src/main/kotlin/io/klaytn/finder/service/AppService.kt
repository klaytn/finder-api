package io.klaytn.finder.service

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.klaytn.finder.domain.mysql.set4.*
import io.klaytn.finder.infra.cache.CacheName
import io.klaytn.finder.infra.cache.CacheUtils
import io.klaytn.finder.infra.cache.CacheValue
import io.klaytn.finder.infra.db.DbConstants
import org.apache.commons.collections4.map.CaseInsensitiveMap
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Service
class AppService(
    private val appPricePlanCachedService: AppPricePlanCachedService,
    private val appUserCachedService: AppUserCachedService,
    private val appUserKeyCachedService: AppUserKeyCachedService,
) {
    private var appPricePlanCache: Cache<String, List<AppPricePlan>> = Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .maximumSize(50)
        .build()

    fun getAppUserByAccessKey(accessKey: String): AppUser? {
        val appUserKey = appUserKeyCachedService.getAppUserKeyIdByAccessKey(accessKey)?.let {
            appUserKeyCachedService.getAppUserKey(it)
        }
        if(appUserKey == null || appUserKey.deactivatedAt != null) {
            return null
        }

        val appUser = appUserCachedService.getAppUser(appUserKey.appUserId)
        return if(appUser != null && appUser.deactivatedAt == null) {
            appUser
        } else {
            null
        }
    }

    /**
     * Returns a list of appUserKeys owned by a specific user.
     */
    fun getAppUserKeys(appUserId: Long, activatedOnly: Boolean) =
        appUserKeyCachedService.getAppUserKeyIds(appUserId).run {
            val appUserKeys = appUserKeyCachedService.getAppUserKeys(this).values
                if(activatedOnly) {
                    appUserKeys.filter { it.activatedAt != null }.toList()
                } else {
                    appUserKeys
                }
        }

    /**
     * Deletes the appUserKey owned by a specific user.
     */
    @Transactional(DbConstants.set4TransactionManager)
    fun removeAppUserKey(appUserId: Long, appUserKeyId: Long) =
        appUserKeyCachedService.deleteAppUserKey(appUserId, appUserKeyId)


    /**
     * Returns a specific fee plan.
     * - If it doesn't exist, return the default (all_request_limit).
     */
    fun getAppPricePlan(appPricePlanId: Long) =
        getAllAppPricePlans().firstOrNull { it.id == appPricePlanId } ?: AppPricePlan.getAllRequestLimitPlan()

    /**
     * List of active fee plans to be displayed to the user.
     */
    fun getActivatedAppPricePlans() =
        getAllAppPricePlans().filter { it.deactivatedAt == null && !it.hidden }.sortedBy { it.displayOrder }

    /**
     * List of all fee plans regardless of their activation status.
     */
    fun getAllAppPricePlans(): List<AppPricePlan> {
        val appPricePlans = appPricePlanCache.getIfPresent(CacheValue.ALL)
        if(!appPricePlans.isNullOrEmpty()) {
            return appPricePlans
        }

        return appPricePlanCachedService.getAppPricePlans()
            .sortedBy { it.displayOrder }
            .toList()
            .also { appPricePlanCache.put(CacheValue.ALL, it) }
    }
}

@Service
class AppUserCachedService(
    private val cacheUtils: CacheUtils,
    private val appUserRepository: AppUserRepository
) {
    fun getAppUser(appUserId: Long): AppUser? {
        val appUsers = getAppUserMap(setOf(appUserId))

        return if (appUsers.size == 1) {
            appUsers[appUserId]
        } else {
            null
        }
    }

    fun getAppUserMap(searchAppUserIds: Collection<Long>) =
        CaseInsensitiveMap(
            cacheUtils.getEntities(CacheName.APP_USER,
                AppUser::class.java,
                AppUser::id,
                searchAppUserIds,
                appUserRepository::findAllByIdIn)
        )
}

@Service
class AppUserKeyCachedService(
    private val cacheUtils: CacheUtils,
    private val appUserKeyRepository: AppUserKeyRepository,
) {
    fun getAppUserKeys(searchIds: List<Long>) =
        cacheUtils.getEntities(
            CacheName.APP_USER_KEY_BY_ID,
            AppUserKey::class.java,
            AppUserKey::id,
            searchIds,
            appUserKeyRepository::findAllByIdIn)

    fun getAppUserKey(id: Long): AppUserKey? {
        val appUserKeyMap = getAppUserKeys(listOf(id))

        return if (appUserKeyMap.size == 1) {
            appUserKeyMap[id]
        } else {
            null
        }
    }

    @Cacheable(cacheNames = [CacheName.APP_USER_KEY_ID_BY_ACCESS_KEY], key = "#accessKey", unless = "#result == null")
    fun getAppUserKeyIdByAccessKey(accessKey: String) =
        appUserKeyRepository.findByAccessKey(accessKey)?.id

    @Cacheable(cacheNames = [CacheName.APP_USER_KEY_IDS_BY_APP_USER_ID], key = "#appUserId", unless = "#result == null")
    fun getAppUserKeyIds(appUserId: Long) =
        appUserKeyRepository.findAllByAppUserId(appUserId).map { it.id }

    @CacheEvict(cacheNames = [CacheName.APP_USER_KEY_IDS_BY_APP_USER_ID], key = "#appUserId")
    fun deleteAppUserKey(appUserId: Long, appUserKeyId: Long) =
        appUserKeyRepository.deleteByAppUserIdAndId(appUserId, appUserKeyId)
}

@Service
class AppPricePlanCachedService(
    private val appPricePlanRepository: AppPricePlanRepository,
) {
    @Cacheable(cacheNames = [CacheName.APP_PRICE_PLANS], key = CacheValue.ALL, unless = "#result == null")
    fun getAppPricePlans(): MutableList<AppPricePlan> = appPricePlanRepository.findAll()

    @CacheEvict(cacheNames = [CacheName.APP_PRICE_PLANS], key = CacheValue.ALL)
    fun flush() {
    }
}
