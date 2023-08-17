package io.klaytn.finder.infra.web.interceptor

import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.infra.exception.UnauthorizedRequestException
import io.klaytn.finder.infra.security.FinderAuthenticator
import io.klaytn.finder.infra.security.auth.UserSession
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsUtils
import org.springframework.web.method.HandlerMethod
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class UserSessionInterceptor(
    private val userSession: UserSession,
    private val finderAuthenticator: FinderAuthenticator,
) : HandlerInterceptorSupport() {
    private val logger = logger(this::class.java)

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any)
    : Boolean {
        if(CorsUtils.isPreFlightRequest(request)) {
            return super.preHandle(request, response, handler)
        }

        val handlerMethod = handler as HandlerMethod
        userSession.setHandlerMethod(handlerMethod)

        // Authentication
        val authorizationHeader = request.getHeaders(HttpHeaders.AUTHORIZATION)
            .toList()
            .find { it.startsWith("Bearer ", ignoreCase = true) }

        if (!authorizationHeader.isNullOrBlank()) {
            val accessToken = authorizationHeader.split(" ")[1].trim()
            try {
                userSession.user = finderAuthenticator.authenticate(request, accessToken)
                userSession.accessToken = accessToken
            } catch (e: Exception) {
                logger.warn("fail to validate access_token($accessToken) caused by ${e.message}")
                throw UnauthorizedRequestException("[Invalid access token] $accessToken\r\ncaused by ${e.message}")
            }
        }

        val authConfig = getAuthConfig(handlerMethod)
        if (!userSession.authenticated && authConfig.userRequired) {
            throw UnauthorizedRequestException("unauthorized user access.")
        }
        return super.preHandle(request, response, handler)
    }
}