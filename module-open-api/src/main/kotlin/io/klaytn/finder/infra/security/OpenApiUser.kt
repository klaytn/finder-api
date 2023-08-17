package io.klaytn.finder.infra.security

import io.klaytn.finder.domain.mysql.set4.AppPricePlan
import io.klaytn.finder.domain.mysql.set4.AppUser

data class OpenApiUser(
    val appUser: AppUser,
    val appPricePlan: AppPricePlan,
) {
    fun getAppUserId() = appUser.id
    fun getAppPricePlanId() = appPricePlan.id
}