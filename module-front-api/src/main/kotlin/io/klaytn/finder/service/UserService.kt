package io.klaytn.finder.service

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.klaytn.finder.domain.mysql.set1.User
import io.klaytn.finder.domain.mysql.set1.UserRepository
import io.klaytn.finder.infra.cache.CacheName
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class UserService(
    private val userCachedService: UserCachedService,
) {
    private var userCache: Cache<String, User> = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.HOURS)
        .maximumSize(10)
        .build()

    fun getUserByAccessKey(accessKey: String) =
        userCache.getIfPresent(accessKey) ?: userCachedService.getUserByAccessKey(accessKey)?.also {
            userCache.put(accessKey, it)
        }
}

@Service
class UserCachedService(
    private val userRepository: UserRepository,
) {
    @Cacheable(cacheNames = [CacheName.USER_BY_ACCESS_KEY], key = "#accessKey", unless = "#result == null")
    fun getUserByAccessKey(accessKey: String) = userRepository.findByAccessKey(accessKey)
}
