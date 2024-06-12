package io.klaytn.finder.interfaces.rest.api.view.mapper

import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.domain.common.KaiaUserType
import org.springframework.stereotype.Component
import io.klaytn.finder.interfaces.rest.api.view.model.kaiauser.KaiaUserSignupView
import io.klaytn.finder.domain.mysql.set1.KaiaUser
import org.springframework.security.crypto.password.PasswordEncoder
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