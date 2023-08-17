package io.klaytn.finder.interfaces.rest.api.websocket

import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.security.FinderAuthenticator
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

@Profile(ServerMode.API_MODE)
@Component
class FinderHomeHandshakeAuthInterceptor(
    private val finderAuthenticator: FinderAuthenticator,
) : HandshakeInterceptor {
    private val logger = logger(this::class.java)

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>,
    ): Boolean {
        if (request !is ServletServerHttpRequest) {
            logger.debug("not supported request type. (${request.javaClass}).")
            return fail(response)
        }

        val servletRequest = request.servletRequest
        val authorizationHeader = servletRequest.getHeaders(HttpHeaders.AUTHORIZATION)
            .toList().find { it.startsWith("Bearer ", ignoreCase = true) }
        if (!authorizationHeader.isNullOrBlank()) {
            val accessToken = authorizationHeader.split(" ")[1].trim()
            logger.debug("user access token is $accessToken")

            try {
                finderAuthenticator.authenticate(servletRequest, accessToken)
                return success()
            } catch (e: Exception) {
                logger.error("occurred exception during validate user access token", e)
            }
        } else {
            logger.warn("not found user access token")
        }

        return fail(response)
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?,
    ) {
    }

    private fun fail(response: ServerHttpResponse): Boolean {
        response.setStatusCode(HttpStatus.FORBIDDEN)
        return false
    }

    private fun success() = true
}