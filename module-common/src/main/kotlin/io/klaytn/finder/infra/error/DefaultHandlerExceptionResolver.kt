package io.klaytn.finder.infra.error

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerExceptionResolver

class DefaultHandlerExceptionResolver(
        private val errorHandler: ExceptionHandler,
) : HandlerExceptionResolver {
    override fun resolveException(
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse,
            handler: Any?,
            exception: Exception,
    ) = errorHandler.getErrorResultView(httpServletRequest, exception)
}
