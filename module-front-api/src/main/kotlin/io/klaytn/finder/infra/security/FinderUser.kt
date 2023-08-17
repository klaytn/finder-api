package io.klaytn.finder.infra.security

import io.klaytn.finder.domain.mysql.set1.UserType

data class FinderUser(
    val id: Long,
    private val username: String,
    private val userType: UserType,
)