package io.klaytn.finder.service

import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.klaytn.finder.config.ClientProperties
import io.klaytn.finder.domain.common.KaiaUserType
import io.klaytn.finder.domain.mysql.set1.KaiaUser
import io.klaytn.finder.domain.mysql.set1.KaiaUserEmailAuth
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
import java.security.MessageDigest
import java.time.Instant
import java.util.*

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
    private val jwtSecret = clientProperties.keys["jwt-secret"]!!

    fun signUp(kaiaUserSignupData: KaiaUserSignupView): Boolean {
        if (!isValidEmail(kaiaUserSignupData.email)) {
            throw InvalidRequestException("Invalid email address")
        }

        if (kaiaUserRepository.existsByEmail(kaiaUserSignupData.email)) {
            throw InvalidRequestException("Email already exists")
        }

        if (kaiaUserRepository.existsByName(kaiaUserSignupData.name)) {
            throw InvalidRequestException("Name already exists")
        }

        val kaiaUserData: KaiaUser = kaiaUserSignupViewToMapper.transform(kaiaUserSignupData)
        val kaiaUserInfo: KaiaUser = kaiaUserRepository.save(kaiaUserData)
        val userEmailAuthEntity = kaiaUserEmailAuthMapper.transform(kaiaUserInfo)
        val kaiaUserEmailAuth: KaiaUserEmailAuth = kaiaUserEmailAuthRepository.save(userEmailAuthEntity)

        this.sendBySendGrid(kaiaUserEmailAuth.email, kaiaUserEmailAuth.jwtToken)

        return true
    }

    fun String.toSHA256(): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(this.toByteArray(Charsets.UTF_8))
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    //  TODO: email template
    //  TODO: redirect to frontend ??
    fun sendBySendGrid(email: String, jwtToken: String) {
        val from = Email("noreply@klaytnfinder.io")
        val subject = "Sending with SendGrid is Fun"
        val to = Email(email)
        val content = Content("text/plain", "Click Here {FRONT_END_URL}?jwtToken=$jwtToken")

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
        val parseJWT = SignedJWT.parse(jwtToken)
        val hashedKey = jwtSecret.toSHA256()
        if (!parseJWT.verify(MACVerifier(hashedKey))) {
            throw InvalidRequestException("Invalid JWT token")
        }

        val claims = parseJWT.jwtClaimsSet
        val cTime = Date()
        if (claims.expirationTime.before(cTime)) {
            throw InvalidRequestException("Expired JWT token")
        }

        val userId = claims.getLongClaim("userId")
        val email = claims.getStringClaim("email")

        val kaiaUserEmailAuth = kaiaUserEmailAuthRepository.findByJwtToken(jwtToken)
            ?: throw InvalidRequestException("Invalid token")
        val kaiaUser: KaiaUser = kaiaUserRepository.findById(userId)
            .orElseThrow { InvalidRequestException("User not found") }

        when {
            kaiaUser.email != email -> {
                throw InvalidRequestException("Invalid email")
            }

            kaiaUser.status === KaiaUserType.ACTIVE -> {
                throw InvalidRequestException("Already verified")
            }

            kaiaUser.status === KaiaUserType.DEACTIVATED -> {
                throw InvalidRequestException("Deactivated user")
            }
        }

        kaiaUserEmailAuthRepository.updateIsVerifiedByUserId(kaiaUserEmailAuth.userId, true)

        kaiaUser.status = KaiaUserType.ACTIVE
        kaiaUserRepository.save(kaiaUser)

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