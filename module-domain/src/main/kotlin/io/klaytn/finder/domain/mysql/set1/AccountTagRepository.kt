package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.mysql.BaseRepository
import org.springframework.stereotype.Repository

@Repository
interface AccountTagRepository : BaseRepository<AccountTag> {
    fun findAccountTagByTag(tag: String): AccountTag?
    fun deleteAccountTagByTag(tag: String): Long
}