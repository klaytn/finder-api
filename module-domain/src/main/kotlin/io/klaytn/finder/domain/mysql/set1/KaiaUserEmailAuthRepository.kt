package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.common.KaiaUserEmailAuthVerificationType
import io.klaytn.finder.domain.mysql.BaseRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface KaiaUserEmailAuthRepository : BaseRepository<KaiaUserEmailAuth> {
    fun findByJwtToken(jwtToken: String): KaiaUserEmailAuth?

    @Modifying
    @Transactional
    @Query("UPDATE KaiaUserEmailAuth k SET k.isVerified = :isVerified WHERE k.userId = :userId")
    fun updateIsVerifiedByUserId(userId: Long, isVerified: Boolean)
}