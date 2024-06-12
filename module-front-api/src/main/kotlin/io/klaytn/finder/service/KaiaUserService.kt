package io.klaytn.finder.service

import io.klaytn.finder.domain.mysql.set1.KaiaUser
import io.klaytn.finder.domain.mysql.set1.KaiaUserRepository
import io.klaytn.finder.infra.exception.InvalidRequestException
import io.klaytn.finder.interfaces.rest.api.view.mapper.KaiaUserSignupViewMapper
import io.klaytn.finder.interfaces.rest.api.view.model.kaiauser.KaiaUserSignupView
import org.springframework.stereotype.Service
import java.util.regex.Pattern

@Service
class KaiaUserService(
    private val kaiaUserRepository: KaiaUserRepository,
    private val kaiaUserSignupViewToMapper: KaiaUserSignupViewMapper
) {
    fun signUp(kaiaUser: KaiaUserSignupView): Boolean {
        if (!isValidEmail(kaiaUser.email)) {
            throw InvalidRequestException("Invalid email address")
        }

        if (kaiaUserRepository.existsByEmail(kaiaUser.email)) {
            throw InvalidRequestException("Email already exists")
        }

        if (kaiaUserRepository.existsByName(kaiaUser.name)) {
            throw InvalidRequestException("Name already exists")
        }

        val userEntity = kaiaUserSignupViewToMapper.transform(kaiaUser)

        kaiaUserRepository.save(userEntity)
        return true
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        )
        return emailPattern.matcher(email).matches()
    }
}