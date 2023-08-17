package io.klaytn.finder.domain.mysql.set4

import io.klaytn.finder.domain.mysql.BaseRepository
import io.klaytn.finder.domain.mysql.EntityId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
interface AppUserKeyRepository : BaseRepository<AppUserKey> {
    fun findByAccessKey(accessKey: String): EntityId?
    fun findAllByAppUserId(appUserId: Long): List<EntityId>
    fun deleteByAppUserIdAndId(appUserId: Long, id: Long): Long
}
