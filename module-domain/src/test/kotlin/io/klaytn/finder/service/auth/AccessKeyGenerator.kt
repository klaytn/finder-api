package io.klaytn.finder.service.auth

import java.util.*
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator

class AccessKeyGenerator {
    @Test
    fun generate() {
        val generator = Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 32)
        println("generator.generateKey().lowercase()")
        println(generator.generateKey().lowercase())
    }
}
