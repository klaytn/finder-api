package io.klaytn.finder.interfaces.rest.api.view.mapper

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.config.ClientProperties
import io.klaytn.finder.domain.common.KaiaUserEmailAuthType
import io.klaytn.finder.domain.common.KaiaUserType
import io.klaytn.finder.domain.mysql.set1.KaiaUser
import io.klaytn.finder.domain.mysql.set1.KaiaUserEmailAuth
import io.klaytn.finder.domain.mysql.set1.KaiaUserLoginHistory
import io.klaytn.finder.interfaces.rest.api.view.model.kaiauser.KaiaUserAccountView
import io.klaytn.finder.interfaces.rest.api.view.model.kaiauser.KaiaUserSignupView
import io.klaytn.finder.interfaces.rest.api.view.model.kaiauser.KaiaUserView
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.time.Instant
import java.util.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

@Component
class KaiaUserSignupViewMapper(private val passwordEncoder: PasswordEncoder) : Mapper<KaiaUserSignupView, KaiaUser> {
    override fun transform(source: KaiaUserSignupView): KaiaUser {
        val encryptedPassword = passwordEncoder.encode(source.password)

        return KaiaUser(
            name = source.name,
            email = source.email,
            password = encryptedPassword,
            profileImage = source.profileImage,
            isSubscribed = source.isSubscribed,
            registerTimestamp = Instant.now().epochSecond.toInt(),
            status = KaiaUserType.UNVERIFIED
        )
    }
}

@Component
class KaiaUserViewMapper() : Mapper<KaiaUser, KaiaUserView> {
    override fun transform(source: KaiaUser): KaiaUserView {
        return KaiaUserView(
            name = source.name,
            email = source.email,
            profileImage = source.profileImage,
            isSubscribed = source.isSubscribed,
            status = source.status,
            registerTimestamp = source.registerTimestamp
        )
    }
}

@Component
class KaiaUserEmailAuthMapper(private val clientProperties: ClientProperties) : Mapper<KaiaUser, KaiaUserEmailAuth> {
    private val jwtSecret = clientProperties.keys["jwt-secret"]!!

    override fun transform(source: KaiaUser): KaiaUserEmailAuth {
        val email = source.email
        val userId = source.id

        val authType = KaiaUserEmailAuthType.SIGNUP
        val secretKey = jwtSecret.toSHA256()
        val currentTime = Date(Instant.now().toEpochMilli())
        val expirationTime = Date(Instant.now().toEpochMilli() + 1000 * 60 * 60)

        val payload = JWTClaimsSet.Builder()
            .claim("userId", userId)
            .claim("email", email)
            .issueTime(currentTime)
            .expirationTime(expirationTime)
            .build()

        val signedJWT = SignedJWT(JWSHeader(JWSAlgorithm.HS256), payload)
        signedJWT.sign(MACSigner(secretKey))
        val jwtToken = signedJWT.serialize()

        return KaiaUserEmailAuth(
            email,
            userId,
            authType,
            jwtToken,
            isVerified = false
        )
    }

    fun String.toSHA256(): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(this.toByteArray(Charsets.UTF_8))
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}

@Component
class KaiaUserLoginHistoryMapper : Mapper<KaiaUser, KaiaUserLoginHistory> {
    override fun transform(source: KaiaUser): KaiaUserLoginHistory {
        val currentTimestamp = (System.currentTimeMillis() / 1000).toInt()

        return KaiaUserLoginHistory(
            userId = source.id,
            timestamp = currentTimestamp
        )
    }
}

@Component
class KaiaUserAccountViewMapper {
    fun transform(user: KaiaUser, lastLoginHistory: KaiaUserLoginHistory?): KaiaUserAccountView {
        val lastLogin = lastLoginHistory?.timestamp?.toUtcString() ?: "No recent login"
        return KaiaUserAccountView(
            name = user.name,
            email = user.email,
            lastLogin = lastLogin
        )
    }

    fun Int.toUtcString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(this.toLong() * 1000))
    }
}