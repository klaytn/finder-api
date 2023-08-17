package io.klaytn.finder.infra.exception

import io.klaytn.finder.infra.error.ApplicationErrorType
import javax.servlet.http.HttpServletRequest

class NotFoundUrlException(throwable: Throwable, httpServletRequest: HttpServletRequest) :
    ApplicationErrorException(errorType, throwable, getRequestUrl(httpServletRequest)) {
    companion object {
        private val errorType = ApplicationErrorType.NOT_FOUND_URL

        private fun getRequestUrl(httpServletRequest: HttpServletRequest) =
            "${httpServletRequest.method} ${httpServletRequest.requestURL}"
    }
}
