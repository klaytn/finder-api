package io.klaytn.finder.service

import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jwt.SignedJWT
import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import io.klaytn.finder.config.ClientProperties
import io.klaytn.finder.domain.common.KaiaUserType
import io.klaytn.finder.domain.mysql.set1.*
import io.klaytn.finder.infra.exception.InvalidRequestException
import io.klaytn.finder.infra.redis.RedisKeyManager
import io.klaytn.finder.interfaces.rest.api.view.mapper.*
import io.klaytn.finder.interfaces.rest.api.view.model.kaiauser.*
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.io.IOException
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

@Service
class KaiaUserService(
    private val kaiaUserRepository: KaiaUserRepository,
    private val kaiaUserSignupViewToMapper: KaiaUserSignupViewMapper,
    private val kaiaUserEmailAuthMapper: KaiaUserEmailAuthMapper,
    private val passwordEncoder: PasswordEncoder,
    private val clientProperties: ClientProperties,
    private val kaiaUserEmailAuthRepository: KaiaUserEmailAuthRepository,
    private val kaiaUserLoginHistoryRepository: KaiaUserLoginHistoryRepository,
    private val redisKeyManager: RedisKeyManager,
    private val redisTemplate: RedisTemplate<String, String>,
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

    fun signIn(kaiaUserSignIn: KaiaUserSignInView): Pair<KaiaUserView, String> {
        val kaiaUser: KaiaUser = kaiaUserRepository.findByName(kaiaUserSignIn.userName)
            ?: throw InvalidRequestException("User not found")

        if (!this.verifyPassword(kaiaUserSignIn.password, kaiaUser.password)) {
            throw InvalidRequestException("Invalid password")
        }

        val userInfo = mapOf(
            "userId" to kaiaUser.id.toString(),
            "userName" to kaiaUser.name
        )
        val sessionId = generateRandomSessionId()

        redisTemplate.opsForHash<String, String>().putAll(redisKeyManager.chainKaiaUserSession(sessionId), userInfo)
        redisTemplate.expire(redisKeyManager.chainKaiaUserSession(sessionId), 24, TimeUnit.HOURS)

        val loginHistoryMapper = KaiaUserLoginHistoryMapper()
        val loginHistory = loginHistoryMapper.transform(kaiaUser)

        kaiaUserLoginHistoryRepository.save(loginHistory)

        return Pair(KaiaUserViewMapper().transform(kaiaUser), sessionId)
    }

    fun signOut(sessionKey: String) {
        redisTemplate.delete(redisKeyManager.chainKaiaUserSession(sessionKey))
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

    fun account(): KaiaUserAccountView {
        //TODO : get user from security context
        //Fetch user information via redis
        //Dummy data
        val userName = "stephen"
        val kaiaUser: KaiaUser = kaiaUserRepository.findByName(userName)
            ?: throw InvalidRequestException("User not found")

        val userLastLogin: KaiaUserLoginHistory? =
            kaiaUserLoginHistoryRepository.findTopByUserIdOrderByTimestampDesc(kaiaUser.id)

        val accountViewMapper = KaiaUserAccountViewMapper()
        return accountViewMapper.transform(kaiaUser, userLastLogin)
    }

    fun changePassword(kaiaUserChangePasswordView: KaiaUserChangePasswordView): Boolean {

        val kaiaUser: KaiaUser = kaiaUserRepository.findByName(kaiaUserChangePasswordView.name)
            ?: throw InvalidRequestException("User not found")


        if (!passwordEncoder.matches(kaiaUserChangePasswordView.oldPassword, kaiaUser.password)) {
            throw InvalidRequestException("Current password is incorrect")
        }


        if (kaiaUserChangePasswordView.newPassword != kaiaUserChangePasswordView.confirmPassword) {
            throw InvalidRequestException("New password and confirmation password do not match")
        }

        //TODO: password policy
        if (kaiaUserChangePasswordView.newPassword.length < 3) {
            throw InvalidRequestException("New password must be at least 3 characters long")
        }


        kaiaUser.password = passwordEncoder.encode(kaiaUserChangePasswordView.newPassword)
        kaiaUserRepository.save(kaiaUser)

        return true
    }

    fun deleteAccount(deleteAccountView: KaiaUserDeleteAccountView) {
        val userName = deleteAccountView.name
        val password = deleteAccountView.password

        val kaiaUser: KaiaUser = kaiaUserRepository.findByName(userName)
            ?: throw InvalidRequestException("User not found")

        if (!passwordEncoder.matches(password, kaiaUser.password)) {
            throw InvalidRequestException("Incorrect password")
        }

        kaiaUser.status = KaiaUserType.DEACTIVATED
        kaiaUser.deletedAt = LocalDateTime.now()

        kaiaUserRepository.save(kaiaUser)

    }

    fun loginHistory(): List<String> {
        //TODO : get user from security context
        //Fetch user information via redis
        //Dummy data
        val userName = "jayce"
        val kaiaUser: KaiaUser = kaiaUserRepository.findByName(userName)
            ?: throw InvalidRequestException("User not found")

        val loginHistoryList: List<KaiaUserLoginHistory> =
            kaiaUserLoginHistoryRepository.findTop5ByUserIdOrderByTimestampDesc(kaiaUser.id)

        return loginHistoryList.map { it.timestamp.toString() }
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

    private fun generateRandomSessionId(): String {
        return UUID.randomUUID().toString()
    }
}