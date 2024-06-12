package io.klaytn.finder.service

import io.klaytn.finder.domain.mysql.set1.KaiaUser
import io.klaytn.finder.domain.mysql.set1.KaiaUserRepository
import org.springframework.stereotype.Service

@Service
class KaiaUserService(
    private val kaiaUserRepository: KaiaUserRepository,
) {
    fun signUp(kaiaUser: KaiaUser): Boolean {
        kaiaUserRepository.save(kaiaUser)
        return true
    }
}