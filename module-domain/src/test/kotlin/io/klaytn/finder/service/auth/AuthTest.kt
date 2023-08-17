package io.klaytn.finder.service.auth

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.math.BigInteger
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.util.*
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate

class AuthTest {
    @Test
    fun test() {
        val accessKey = "qa3ti3akkshyyjlef5uauk8t0trxykwe5fgubuaq"
        val secretKey = "4nd9lnzhkvcnjt5yhtdiyjro6peajkanfvwfzjda"

        val timestamp = Instant.now().toEpochMilli()
        val searchString =
                mapOf("_t" to timestamp.toString())
                        .map { (k, v) ->
                            val key = URLEncoder.encode(k, StandardCharsets.UTF_8)
                            val value = URLEncoder.encode(v, StandardCharsets.UTF_8)
                            "$key=$value"
                        }
                        .reduce { p1, p2 -> "$p1&$p2" }

        val md = MessageDigest.getInstance("SHA-512")
        md.update(searchString.toByteArray())
        val hex = String.format("%0128x", BigInteger(1, md.digest()))

        val payload =
                JWTClaimsSet.Builder()
                        .claim("access_key", accessKey)
                        .claim("nonce", UUID.randomUUID().toString())
                        .claim("query_hash", hex)
                        .claim("query_hash_alg", "SHA-512")
                        .build()

        val signedJWT = SignedJWT(JWSHeader(JWSAlgorithm.HS256), payload)
        signedJWT.sign(MACSigner(secretKey))
        val jwt = signedJWT.serialize()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("Authorization", "Bearer $jwt")

        val entity: HttpEntity<String> = HttpEntity<String>(headers)
        val result =
                RestTemplate()
                        .exchange(
                                "https://cypress-api.klaytnfinder.io/api/v1/blocks?$searchString",
                                HttpMethod.GET,
                                entity,
                                String::class.java
                        )
        println(result)
    }
}
