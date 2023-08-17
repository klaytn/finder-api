package io.klaytn.finder.infra.security.auth

import io.klaytn.finder.infra.security.FinderUser
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import org.springframework.web.method.HandlerMethod
import java.util.*
import javax.servlet.http.HttpServletRequest

/**
 * Since it's used in RequestScope, all fields and properties must have the 'open' attribute.
 * - When RequestScope is declared, a proxy object is created, and if 'open' is not declared, it won't work properly.
 * - Fortunately, when you declare the Component Annotation and add kotlin.spring, 'open' is automatically set.
 */
@Component
@RequestScope
class UserSession {
    var user: FinderUser? = null
    var accessToken: String? = null
    var remoteAddress: String? = null
    var locale: Locale? = null
    var userAgent: String? = null
    var requestController: String? = null
    var requestMethod: String? = null

    constructor(httpServletRequest: HttpServletRequest) {
        userAgent = httpServletRequest.getHeader(HttpHeaders.USER_AGENT)
        remoteAddress = httpServletRequest.remoteAddr
        locale = httpServletRequest.locale
    }

    fun setHandlerMethod(handlerMethod: HandlerMethod) {
        requestController = handlerMethod.beanType.name
        requestMethod = handlerMethod.method.name
    }

    val authenticated: Boolean
        get() = user != null
}