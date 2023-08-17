package io.klaytn.finder.infra.web.interceptor

import io.klaytn.finder.infra.security.auth.Auth
import io.klaytn.finder.infra.security.auth.AuthConfig
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

abstract class HandlerInterceptorSupport : HandlerInterceptor {
    protected fun getAuthConfig(handlerMethod: HandlerMethod): AuthConfig {
        return AuthConfig.of(getAnnotation(handlerMethod, Auth::class.java))
    }

    protected fun <A : Annotation?> getAnnotation(handlerMethod: HandlerMethod, annotationType: Class<A>): A {
        return handlerMethod.getMethodAnnotation(annotationType) ?: handlerMethod.beanType.getAnnotation(annotationType)
    }
}