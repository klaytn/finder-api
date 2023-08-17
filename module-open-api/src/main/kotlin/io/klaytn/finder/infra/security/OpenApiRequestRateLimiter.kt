package io.klaytn.finder.infra.security

import io.klaytn.finder.domain.mysql.set4.AppPricePlan
import io.klaytn.finder.infra.exception.ApiRequestQuotaExceededException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class OpenApiRequestRateLimiter(
    private val redisTemplate: RedisTemplate<String, String>,
) {
    private val keyPrefix = "finder/open-api/request-limits"
    private val dayFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val monthFormatter = DateTimeFormatter.ofPattern("yyyyMM")

    fun getCurrentRequestsOfAppUser(appUserId: Long) =
        OpenApiAppUserRequests(
            requestPerSecond = getCurrentRequestsOfAppUser(requestPerSecondKey(appUserId)),
            requestPerDay = getCurrentRequestsOfAppUser(requestPerDayKey(appUserId))
        )

    fun getDailyRequestStatsOfAppUser(appUserId: Long): Map<String, OpenApiAppUserRequests> {
        val today = LocalDate.now()
        val requestStatsOfAppUser = mutableMapOf<String, OpenApiAppUserRequests>()
        IntRange(0, 6).forEach {
            val date = today.minusDays(it.toLong())
            requestStatsOfAppUser[date.format(dayFormatter)] =
                OpenApiAppUserRequests(
                    requestPerDay = getCurrentRequestsOfAppUser(requestPerDayKey(appUserId, date))
                )
        }
        return requestStatsOfAppUser
    }

    fun getMonthlyRequestStatsOfAppUser(appUserId: Long): Map<String, OpenApiAppUserRequests> {
        val today = LocalDate.now()
        val requestStatsOfAppUser = mutableMapOf<String, OpenApiAppUserRequests>()
        IntRange(0, 11).forEach {
            val date = today.minusMonths(it.toLong())
            requestStatsOfAppUser[date.format(monthFormatter)] =
                OpenApiAppUserRequests(
                    requestPerMonth = getCurrentRequestsOfAppUser(requestPerMonthKey(appUserId, date))
                )
        }
        return requestStatsOfAppUser
    }

    fun getCurrentRequestsOfIp(ipAddress: String) =
        OpenApiIpRequests(redisTemplate.opsForValue().get(requestPerSecondKey(ipAddress))?.toLong() ?: 0L)


    fun checkRequestLimitPerIp(ipAddress: String, requestLimitPerIp: Long) {
        val currentRequests = getCurrentRequestsOfIp(ipAddress)
        checkRequestLimit(
            requestLimitType = "requestPerIp",
            requestLimit = requestLimitPerIp,
            currentRequests = currentRequests.requestPerSecond ?: 0L
        )
    }

    fun checkRequestLimit(appUserId: Long, appPricePlan: AppPricePlan) {
        val openApiUserRequestLimit = getCurrentRequestsOfAppUser(appUserId)

        checkRequestLimit(
            requestLimitType = "requestPerSecond",
            requestLimit = appPricePlan.requestLimitPerSecond,
            currentRequests = openApiUserRequestLimit.requestPerSecond ?: 0L
        )

        if (!appPricePlan.allowLimitOver) {
            checkRequestLimit(
                requestLimitType = "requestPerDay",
                requestLimit = appPricePlan.requestLimitPerDay,
                currentRequests = openApiUserRequestLimit.requestPerDay ?: 0L
            )
        }
    }

    fun increaseCurrentRequest(appUserId: Long, appPricePlan: AppPricePlan) {
        increaseCurrentRequest(requestPerSecondKey(appUserId), Duration.ofSeconds(1L))
        increaseCurrentRequest(requestPerDayKey(appUserId), Duration.ofDays(60L))
        increaseCurrentRequest(requestPerMonthKey(appUserId), Duration.ofDays(365L))
    }

    fun increaseCurrentRequestPerIp(ipAddress: String) {
        increaseCurrentRequest(requestPerSecondKey(ipAddress), Duration.ofSeconds(1L))
    }

    private fun checkRequestLimit(requestLimitType: String, requestLimit: Long, currentRequests: Long) {
        if (currentRequests >= requestLimit) {
            throw ApiRequestQuotaExceededException(requestLimitType, requestLimit)
        }
    }

    private fun increaseCurrentRequest(requestLimitKey: String, requestLimitKeyExpire: Duration) {
        val requestPerMonthValue = redisTemplate.opsForValue().increment(requestLimitKey) ?: 1L
        if (requestPerMonthValue == 1L) {
            redisTemplate.expire(requestLimitKey, requestLimitKeyExpire)
        }
    }

    private fun getCurrentRequestsOfAppUser(requestLimitKey: String) =
        redisTemplate.opsForValue().get(requestLimitKey)?.toLong() ?: 0L

    // -- --------------------------------------------------------------------------------------------------------------
    // -- cache key
    // -- --------------------------------------------------------------------------------------------------------------

    private fun requestPerSecondKey(ipAddress: String) =
        "$keyPrefix/$ipAddress/requestPerSecond"

    private fun requestPerSecondKey(appUserId: Long) =
        "$keyPrefix/$appUserId/requestPerSecond"

    private fun requestPerDayKey(appUserId: Long, localDate: LocalDate) =
        "$keyPrefix/$appUserId/requestPerDay/${dayFormatter.format(localDate)}"

    private fun requestPerMonthKey(appUserId: Long, localDate: LocalDate) =
        "$keyPrefix/$appUserId/requestPerMonth/${monthFormatter.format(localDate)}"

    private fun requestPerDayKey(appUserId: Long) = requestPerDayKey(appUserId, LocalDate.now())

    private fun requestPerMonthKey(appUserId: Long) = requestPerMonthKey(appUserId, LocalDate.now())
}

data class OpenApiAppUserRequests(
    val requestPerSecond: Long? = null,
    val requestPerDay: Long? = null,
    val requestPerMonth: Long? = null,
)

data class OpenApiIpRequests(
    val requestPerSecond: Long,
)