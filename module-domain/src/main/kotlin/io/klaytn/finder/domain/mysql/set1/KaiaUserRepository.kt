package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.mysql.BaseRepository
import org.springframework.stereotype.Repository

@Repository
interface KaiaUserRepository: BaseRepository<KaiaUser> {
    fun existsByEmail(email: String): Boolean
    fun existsByName(name: String): Boolean
}
