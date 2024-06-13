package io.klaytn.finder.service

import com.sendgrid.*
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.*
import io.klaytn.finder.config.ClientProperties
import io.klaytn.finder.domain.mysql.set1.KaiaUser
import io.klaytn.finder.domain.mysql.set1.KaiaUserRepository
import io.klaytn.finder.infra.exception.InvalidRequestException
import io.klaytn.finder.interfaces.rest.api.view.mapper.KaiaUserSignupViewMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.KaiaUserViewMapper
import io.klaytn.finder.interfaces.rest.api.view.model.kaiauser.KaiaUserSignInView
import io.klaytn.finder.interfaces.rest.api.view.model.kaiauser.KaiaUserSignupView
import io.klaytn.finder.interfaces.rest.api.view.model.kaiauser.KaiaUserView
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.regex.Pattern
import java.io.IOException

@Service
class KaiaUserService(
    private val kaiaUserRepository: KaiaUserRepository,
    private val kaiaUserSignupViewToMapper: KaiaUserSignupViewMapper,
    private val passwordEncoder: PasswordEncoder,
    private val clientProperties: ClientProperties
) {
    private val sendgridApiKey = clientProperties.keys["sendgrid-api-key"]!!

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
        // TODO: Create Table for Email Verification
        // TODO: JWT Token for Email Verification

        this.sendBySendGrid(kaiaUser.email)

        return true
    }

    //  TODO: email template
    fun sendBySendGrid(email: String) {
        val from = Email("noreply@klaytnfinder.io")
        val subject = "Sending with SendGrid is Fun"
        val to = Email(email)
        val content = Content("text/plain", "and easy to do anywhere, even with Kotlin stephen jayce top")
        val mail = Mail(from, subject, to, content)
        val sg = SendGrid(sendgridApiKey)
        val request = Request()
        try {
            request.method = Method.POST
            request.endpoint = "mail/send"
            request.body = mail.build()
            val response = sg.api(request)
        } catch (ex: IOException) {
            throw InvalidRequestException("Failed to send email: $ex")
        }
    }

    fun signIn(kaiaUserSignIn: KaiaUserSignInView): KaiaUserView {
        val kaiaUser: KaiaUser = kaiaUserRepository.findByName(kaiaUserSignIn.userName)
            ?: throw InvalidRequestException("User not found")

        if(!this.verifyPassword(kaiaUserSignIn.password, kaiaUser.password)) {
            throw InvalidRequestException("Invalid password")
        }

        return KaiaUserViewMapper().transform(kaiaUser)
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        )
        return emailPattern.matcher(email).matches()
    }

    private fun verifyPassword(rawPassword: String, encryptedPassword: String): Boolean {
        return passwordEncoder.matches(rawPassword, encryptedPassword)
    }
}