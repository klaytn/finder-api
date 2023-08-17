package io.klaytn.finder.infra.web.interceptor

import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.infra.exception.UnauthorizedRequestException
import io.klaytn.finder.infra.security.OpenApiRequestRateLimiter
import io.klaytn.finder.infra.security.OpenApiUser
import io.klaytn.finder.infra.security.auth.UserSession
import io.klaytn.finder.service.AppService
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsUtils
import org.springframework.web.method.HandlerMethod
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class UserSessionInterceptor(
    private val userSession: UserSession,
    private val appService: AppService,
    private val openApiRequestRateLimiter: OpenApiRequestRateLimiter
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
                appService.getAppUserByAccessKey(accessToken)?.let {
                    val appPricePlan = appService.getAppPricePlan(it.appPricePlanId)
                    userSession.user = OpenApiUser(it, appPricePlan)
                }
                userSession.accessToken = accessToken
            } catch (e: Exception) {
                logger.warn("fail to validate access_token($accessToken) caused by ${e.message}")
                throw UnauthorizedRequestException("[Invalid access token] $accessToken")
            }
        }

        val authConfig = getAuthConfig(handlerMethod)
        if(authConfig.userRequired) {
            if (!userSession.authenticated) {
                throw UnauthorizedRequestException("unauthorized user access.")
            }

            if(authConfig.requestLimit) {
                val appUser = userSession.user!!
                openApiRequestRateLimiter.checkRequestLimit(appUser.getAppUserId(), appUser.appPricePlan)
            }
        }
        if (authConfig.requestLimitPerIp) {
            val ipAddress = request.getHeader("X-Forwarded-For") ?: request.remoteAddr
            openApiRequestRateLimiter.checkRequestLimitPerIp(ipAddress, authConfig.requestLimitPerIpPerSecond)
        }
        return super.preHandle(request, response, handler)
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: java.lang.Exception?
    ) {
        userSession.user?.let {
            val handlerMethod = handler as HandlerMethod
            val authConfig = getAuthConfig(handlerMethod)
            if(authConfig.requestLimit) {
                openApiRequestRateLimiter.increaseCurrentRequest(it.getAppUserId(), it.appPricePlan)
            }


        }

        val handlerMethod = handler as HandlerMethod
        val authConfig = getAuthConfig(handlerMethod)
        if (authConfig.requestLimitPerIp) {
            val ipAddress = request.getHeader("X-Forwarded-For") ?: request.remoteAddr
            openApiRequestRateLimiter.increaseCurrentRequestPerIp(ipAddress)
        }
    }
}