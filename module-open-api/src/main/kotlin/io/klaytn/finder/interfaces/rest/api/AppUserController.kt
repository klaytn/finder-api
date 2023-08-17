package io.klaytn.finder.interfaces.rest.api

import io.klaytn.finder.infra.security.OpenApiAppUserRequests
import io.klaytn.finder.infra.security.OpenApiRequestRateLimiter
import io.klaytn.finder.infra.security.auth.Auth
import io.klaytn.finder.infra.security.auth.UserSession
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.api.view.mapper.appuser.AppUserKeyToViewMapper
import io.klaytn.finder.service.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = SwaggerConstant.TAG_PUBLIC)
@Auth(userRequired = true, requestLimit = false)
class AppUserController(
    private val userSession: UserSession,
    private val appService: AppService,
    private val openApiRequestRateLimiter: OpenApiRequestRateLimiter,
    private val appUserKeyToViewMapper: AppUserKeyToViewMapper
) {
    @Operation(
        description = "List of keys owned by the app user."
    )
    @GetMapping("/api/v1/app-users/keys")
    fun getAppUserKeys(
        @RequestParam("activated") activatedOnly: Boolean = true
    ) =
        appService.getAppUserKeys(userSession.appUserId!!, activatedOnly).map {
            appUserKeyToViewMapper.transform(it)
        }

    @Operation(
        description = "Retrieve the allocated request count for the app user."
    )
    @GetMapping("/api/v1/app-users/request-quotas")
    fun getRequestQuotasOfAppUser() =
        userSession.user?.appPricePlan?.let {
            OpenApiAppUserRequests(
                requestPerSecond = it.requestLimitPerSecond,
                requestPerDay = it.requestLimitPerDay)
        }

    @Operation(
        description = "Retrieve the remaining request count for the app user."
    )
    @GetMapping("/api/v1/app-users/request-quotas/remaining")
    fun getRequestRemainingQuotasOfAppUser() =
        userSession.user?.appPricePlan?.let {
            val currentRequest = getCurrentRequestsOfAppUser()
            val remainingRequestPerSecond = it.requestLimitPerSecond - (currentRequest.requestPerSecond ?: 0L)
            val remainingRequestPerDay = it.requestLimitPerDay - (currentRequest.requestPerDay ?: 0L)
            OpenApiAppUserRequests(
                requestPerSecond = remainingRequestPerSecond,
                requestPerDay = if(remainingRequestPerDay >= 0) remainingRequestPerDay else 0L
            )
        }

    @Operation(
        description = "Retrieve the total request count for the app user up to the current point."
    )
    @GetMapping("/api/v1/app-users/request-quotas/used")
    fun getCurrentRequestsOfAppUser() =
        openApiRequestRateLimiter.getCurrentRequestsOfAppUser(userSession.appUserId!!)

    @Operation(
        description = "Retrieve the accumulated exceeded request count for the app user up to the current point."
    )
    @GetMapping("/api/v1/app-users/request-quotas/over-used")
    fun getCurrentOverRequestsOfAppUser() =
        userSession.user?.appPricePlan?.let {
            val currentRequest = getCurrentRequestsOfAppUser()
            val overRequestPerDay = (currentRequest.requestPerDay ?: 0L) - it.requestLimitPerDay
            OpenApiAppUserRequests(
                requestPerDay = if(overRequestPerDay >= 0) overRequestPerDay else 0L
            )
        }

    @Operation(
        description = "Retrieve the daily request count for the app user."
    )
    @GetMapping("/api/v1/app-users/request-stats/daily")
    fun getDailyRequestStatsOfAppUser() =
        userSession.appUserId?.let {
            openApiRequestRateLimiter.getDailyRequestStatsOfAppUser(it)
        }

    @Operation(
        description = "Retrieve the monthly request count for the app user."
    )
    @GetMapping("/api/v1/app-users/request-stats/monthly")
    fun getMonthlyRequestStatsOfAppUser() =
        userSession.appUserId?.let {
            openApiRequestRateLimiter.getMonthlyRequestStatsOfAppUser(it)
        }
}
