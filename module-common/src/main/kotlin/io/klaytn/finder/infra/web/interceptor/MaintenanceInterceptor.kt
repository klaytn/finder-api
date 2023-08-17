package io.klaytn.finder.infra.web.interceptor

import io.klaytn.finder.config.dynamic.FinderMaintenanceDynamicConfig
import io.klaytn.finder.infra.error.ApplicationErrorType
import io.klaytn.finder.infra.exception.MaintenanceException
import org.springframework.web.cors.CorsUtils
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class MaintenanceInterceptor(
    private val finderMaintenanceDynamicConfig: FinderMaintenanceDynamicConfig
) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if(CorsUtils.isPreFlightRequest(request)) {
            return super.preHandle(request, response, handler)
        }

        if(finderMaintenanceDynamicConfig.status) {
            val maintenanceMessage = finderMaintenanceDynamicConfig.reason ?: ApplicationErrorType.MAINTENANCE.message
            throw MaintenanceException(maintenanceMessage)
        }
        return super.preHandle(request, response, handler)
    }
}