package io.klaytn.finder.infra.error

import org.springframework.http.HttpStatus

data class ApplicationErrorResponse(
        private val errorType: ApplicationErrorType,
        val title: String?,
        val message: String?,
) {
    val httpStatus: HttpStatus
        get() = errorType.statusCode

    val code: String
        get() = errorType.name

    val defaultTitle: String
        get() = errorType.title

    val defaultMessage: String
        get() = errorType.message
}
