package io.klaytn.finder.service

import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import io.klaytn.finder.config.ClientProperties
import io.klaytn.finder.domain.common.KaiaUserType
import io.klaytn.finder.domain.mysql.set1.KaiaUser
import io.klaytn.finder.domain.mysql.set1.KaiaUserEmailAuthRepository
import io.klaytn.finder.domain.mysql.set1.KaiaUserRepository
import io.klaytn.finder.infra.exception.InvalidRequestException
import io.klaytn.finder.interfaces.rest.api.view.mapper.KaiaUserEmailAuthMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.KaiaUserSignupViewMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.KaiaUserViewMapper
import io.klaytn.finder.interfaces.rest.api.view.model.kaiauser.KaiaUserSignInView
import io.klaytn.finder.interfaces.rest.api.view.model.kaiauser.KaiaUserSignupView
import io.klaytn.finder.interfaces.rest.api.view.model.kaiauser.KaiaUserView
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.io.IOException
import java.util.regex.Pattern

@Service
class KaiaUserService(
    private val kaiaUserRepository: KaiaUserRepository,
    private val kaiaUserSignupViewToMapper: KaiaUserSignupViewMapper,
    private val kaiaUserEmailAuthMapper: KaiaUserEmailAuthMapper,
    private val passwordEncoder: PasswordEncoder,
    private val clientProperties: ClientProperties,
    private val kaiaUserEmailAuthRepository: KaiaUserEmailAuthRepository
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
        // TODO: JWT Token for Email Verification
        val userEmailAuthEntity = kaiaUserEmailAuthMapper.transform(userEntity)
        kaiaUserEmailAuthRepository.save(userEmailAuthEntity)

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

        if (!this.verifyPassword(kaiaUserSignIn.password, kaiaUser.password)) {
            throw InvalidRequestException("Invalid password")
        }

        return KaiaUserViewMapper().transform(kaiaUser)
    }

    fun verifyEmail(jwtToken: String): Boolean {
        // TODO: JWT Token Validation
//        val claims = JwtUtils.parseJwtToken(jwtToken)
//            ?: throw IllegalArgumentException("Invalid JWT token")
//        val userId = claims["userId"] as Long ?: throw IllegalArgumentException("Invalid JWT token")
//        val email = claims["email"] as String ?: throw IllegalArgumentException("Invalid JWT token")
        // TODO: Remove Dummy data
        val userId = 1L
        val email = ""

        val kaiaUser = kaiaUserRepository.findById(userId)
            .orElseThrow { InvalidRequestException("User not found") }

        if (kaiaUser.email != email) {
            throw InvalidRequestException("Invalid email")
        }

        kaiaUser.status = KaiaUserType.ACTIVE
        kaiaUserRepository.save(kaiaUser)

        val kaiaUserEmailAuth = kaiaUserEmailAuthRepository.findByJwtToken(jwtToken)
            ?: throw InvalidRequestException("Invalid token")
        val verify: Boolean = true
        kaiaUserEmailAuthRepository.updateIsVerifiedByUserId(kaiaUserEmailAuth.userId, verify)

        return true
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