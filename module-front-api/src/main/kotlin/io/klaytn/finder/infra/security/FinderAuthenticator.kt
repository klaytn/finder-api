package io.klaytn.finder.infra.security

import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jwt.SignedJWT
import io.klaytn.finder.domain.mysql.set1.UserType
import io.klaytn.finder.infra.exception.UnauthorizedRequestException
import io.klaytn.finder.service.UserService
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.math.BigInteger
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletRequest

@Component
@Profile("devAuthToken")
class DevFinderAuthenticator(userService: UserService, finderApiRateLimiter: FinderApiRateLimiter) :
    FinderAuthenticator(userService, finderApiRateLimiter) {
    override fun authenticate(request: HttpServletRequest, jwt: String) =
        if (jwt.equals("token", true)) {
            FinderUser(0, "admin", UserType.ADMIN)
        } else {
            super.authenticate(request, jwt)
        }
}

@Component
@Profile("!devAuthToken")
class FinderAuthenticator(
    private val userService: UserService,
    private val finderApiRateLimiter: FinderApiRateLimiter,
) {
    private val requestTimestampThreshold = TimeUnit.SECONDS.toMillis(5)

    fun authenticate(request: HttpServletRequest, jwt: String): FinderUser {
        val currentTimestamp = System.currentTimeMillis()
        val signedJWT = SignedJWT.parse(jwt)
        val claims = signedJWT.jwtClaimsSet.claims

        val nonce = getClaim(claims, "nonce")
        val accessKey = getClaim(claims, "access_key")

        val user = userService.getUserByAccessKey(accessKey)
            ?: throw UnauthorizedRequestException("user is not exists: accessKey=$accessKey")

        if (!request.queryString.isNullOrBlank()) {
            val queryHash = getClaim(claims, "query_hash")
            val queryHashAlgorithm = getClaim(claims, "query_hash_alg")

            val md = MessageDigest.getInstance(queryHashAlgorithm)
            md.update(request.queryString.toByteArray())
            val hex = String.format("%0128x", BigInteger(1, md.digest()))

            if (hex != queryHash) {
                throw UnauthorizedRequestException("invalid query hash")
            }
        }

        val timestampParameterValue = request.getParameter("_t")
        if (!timestampParameterValue.isNullOrBlank()) {
//            val requestTimestamp = timestampParameterValue.toLongOrNull() ?: 0L
//            if (currentTimestamp - requestTimestamp >= requestTimestampThreshold) {
//                throw UnauthorizedRequestException("invalid request. _t parameter has old timestamp.")
//            }
        } else {
            throw UnauthorizedRequestException("not found _t parameter.")
        }

        val verifier = MACVerifier(user.secretKey)
        if (!signedJWT.verify(verifier)) {
            throw UnauthorizedRequestException("invalid secret key")
        }
        finderApiRateLimiter.checkLimit(user.id, nonce, user.userType.apiLimit)
        return FinderUser(user.id, user.userName, user.userType)
    }

    private fun getClaim(claims: Map<String, Any>, key: String) =
        claims[key] as String? ?: throw UnauthorizedRequestException("$key is not exists")
}