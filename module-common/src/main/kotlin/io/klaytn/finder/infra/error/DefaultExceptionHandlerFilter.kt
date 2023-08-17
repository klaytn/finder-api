package io.klaytn.finder.infra.error

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

class DefaultExceptionHandlerFilter(
        private val exceptionHandler: ExceptionHandler,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain,
    ) {
        try {
            filterChain.doFilter(request, response)
        } catch (t: Throwable) {
            exceptionHandler.writeErrorResponse(request, response, t)
        }
    }
}
