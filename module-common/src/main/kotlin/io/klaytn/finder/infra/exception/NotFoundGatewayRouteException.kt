package io.klaytn.finder.infra.exception

import io.klaytn.finder.infra.error.ApplicationErrorType
import javax.servlet.http.HttpServletRequest

class NotFoundGatewayRouteException(httpServletRequest: HttpServletRequest) :
    ApplicationErrorException(
        ApplicationErrorType.NOT_FOUND_GATEWAY_ROUTE,
        *arrayOf(getRequestUrl(httpServletRequest))) {

    companion object {
        private fun getRequestUrl(httpServletRequest: HttpServletRequest) =
            "${httpServletRequest.method} ${httpServletRequest.requestURL}"
    }
}
