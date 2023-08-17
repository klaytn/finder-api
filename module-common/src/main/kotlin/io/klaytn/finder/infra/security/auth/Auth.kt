package io.klaytn.finder.infra.security.auth

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Auth(
    /** Indicates whether user authentication is required. */
    val userRequired: Boolean = true,

    /** Specifies whether request rate limiting is enabled. */
    val requestLimit: Boolean = true,

    /** Determines whether rate limiting is applied per IP address. */
    val requestLimitPerIp: Boolean = false,

    /** Specifies the maximum number of requests allowed per second for rate limiting per IP address. */
    val requestLimitPerIpPerSecond: Long = 5L
)
