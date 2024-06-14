package io.klaytn.finder.interfaces.rest.api.view.mapper

import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.domain.common.KaiaUserEmailAuthType
import io.klaytn.finder.domain.common.KaiaUserType
import io.klaytn.finder.domain.mysql.set1.KaiaUser
import io.klaytn.finder.domain.mysql.set1.KaiaUserEmailAuth
import io.klaytn.finder.interfaces.rest.api.view.model.kaiauser.KaiaUserSignupView
import io.klaytn.finder.interfaces.rest.api.view.model.kaiauser.KaiaUserView
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.Instant

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
class KaiaUserEmailAuthMapper() : Mapper<KaiaUser, KaiaUserEmailAuth> {
    override fun transform(source: KaiaUser): KaiaUserEmailAuth {
        val authType = KaiaUserEmailAuthType.SIGNUP
        val jwtToken = "" //TODO

        return KaiaUserEmailAuth(
            email = source.email,
            userId = source.id,
            authType = authType,
            jwtToken = jwtToken,
            isVerified = false
        )
    }
}