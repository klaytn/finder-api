package io.klaytn.finder.infra.security.auth

data class AuthConfig(
        val userRequired: Boolean = true,
        val requestLimit: Boolean = true,
        val requestLimitPerIp: Boolean = false,
        val requestLimitPerIpPerSecond: Long = 5L
) {
    companion object {
        fun of(auth: Auth?): AuthConfig {
            return if (auth != null)
                    AuthConfig(
                            auth.userRequired,
                            auth.requestLimit,
                            auth.requestLimitPerIp,
                            auth.requestLimitPerIpPerSecond
                    )
            else AuthConfig()
        }
    }
}
