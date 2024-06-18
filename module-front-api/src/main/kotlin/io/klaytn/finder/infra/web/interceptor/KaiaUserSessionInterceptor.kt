package io.klaytn.finder.infra.web.interceptor

import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.infra.exception.InvalidRequestException
import io.klaytn.finder.infra.redis.RedisKeyManager
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class KaiaUserSessionInterceptor(
    private val redisTemplate: RedisTemplate<String, String>,
    private val redisKeyManager: RedisKeyManager,
) : HandlerInterceptorSupport() {
    private val logger = logger(this::class.java)

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    )
            : Boolean {
        val cookies = request.cookies ?: throw InvalidRequestException("No cookies present in the request")
        val sessionCookie = cookies.firstOrNull { it.name == "_KAIA.sessionId" }
            ?: throw InvalidRequestException("Session cookie not found")
        val sessionId = sessionCookie.value
        val redisKey = redisKeyManager.chainKaiaUserSession(sessionId)

        if (!redisTemplate.hasKey(redisKey)) {
            val cookie = Cookie(sessionCookie.name, null).apply {
                maxAge = 0
                path = "/"
                isHttpOnly = true
                domain = "localhost"  //TODO: Domain Settings
            }
            response.addCookie(cookie)
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return false
        }
        return super.preHandle(request, response, handler)
    }
}