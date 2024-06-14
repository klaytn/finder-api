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

    fun signUp(kaiaUser: KaiaUserSignupView): Boolean {
//        if (!isValidEmail(kaiaUser.email)) {
//            throw InvalidRequestException("Invalid email address")
//        }
//
//        if (kaiaUserRepository.existsByEmail(kaiaUser.email)) {
//            throw InvalidRequestException("Email already exists")
//        }
//
//        if (kaiaUserRepository.existsByName(kaiaUser.name)) {
//            throw InvalidRequestException("Name already exists")
//        }
//
//        val userEntity = kaiaUserSignupViewToMapper.transform(kaiaUser)

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

//        kaiaUserRepository.save(userEntity)
        // TODO: Create Table for Email Verification
        // TODO: JWT Token for Email Verification
        val secretKey = "4nd9lnzhkvcnjt5yhtdiyjro6peajkanfvwfzjda"
        val currentTime = Date(Instant.now().toEpochMilli())
        val expirationTime = Date(Instant.now().toEpochMilli() + 1000 * 60 * 60)
        val payload = JWTClaimsSet.Builder()
            .claim("name", "stephen")
            .claim("email", "stephen@bisonai.com")
            .issueTime(currentTime)
            .expirationTime(expirationTime)
            .build()

        val signedJWT = SignedJWT(JWSHeader(JWSAlgorithm.HS256), payload)
        signedJWT.sign(MACSigner(secretKey))
        val jwt = signedJWT.serialize()
        println("JWT: $jwt")

        val parseJWT = SignedJWT.parse(jwt)
        val baseString = "exampleStringForSecretKey"
        val hashedKey = baseString.toSHA256()
        println("Signed JWT: $parseJWT")
        if (!parseJWT.verify(MACVerifier(hashedKey))) {
            println("Invalid JWT signature")
        }

        if (parseJWT.verify(MACVerifier(secretKey))) {
            println("Valid JWT issuer")
        }

        // 클레임 검증
        val claims = parseJWT.jwtClaimsSet
        println("Claims: $claims")
        val cTime = Date()
        if (claims.expirationTime.before(cTime)) {
            println("Token expired")
        }
//        this.sendBySendGrid(kaiaUser.email)

        return true
    }

    fun String.toSHA256(): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(this.toByteArray(Charsets.UTF_8))
        return digest.fold("") { str, it -> str + "%02x".format(it) }
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