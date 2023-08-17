package io.klaytn.finder.infra.exception

import io.klaytn.commons.model.exception.ServiceException
import io.klaytn.finder.infra.error.ApplicationErrorType

open class ApplicationErrorException : ServiceException {
    val applicationErrorType: ApplicationErrorType
    val messageArguments = ArrayList<Any>()

    constructor(applicationErrorType: ApplicationErrorType, message: String, cause: Throwable) : super(message, cause) {
        this.applicationErrorType = applicationErrorType
    }

    constructor(applicationErrorType: ApplicationErrorType, message: String) : super(message) {
        this.applicationErrorType = applicationErrorType
    }

    constructor(applicationErrorType: ApplicationErrorType, cause: Throwable) : super(
        applicationErrorType.message,
        cause
    ) {
        this.applicationErrorType = applicationErrorType
    }

    constructor(applicationErrorType: ApplicationErrorType) : super(applicationErrorType.message) {
        this.applicationErrorType = applicationErrorType
    }

    constructor(
        applicationErrorType: ApplicationErrorType,
        vararg arguments: Any,
    ) : super(applicationErrorType.message) {
        this.applicationErrorType = applicationErrorType
        for (arg in arguments) {
            messageArguments.add(arg)
        }
    }

    constructor(applicationErrorType: ApplicationErrorType, cause: Throwable, vararg arguments: Any) : super(
        applicationErrorType.message,
        cause
    ) {
        this.applicationErrorType = applicationErrorType
        for (arg in arguments) {
            messageArguments.add(arg)
        }
    }

    override fun getCode() = applicationErrorType.name
    override fun getTitle() = applicationErrorType.title
    override fun getHttpStatus() = applicationErrorType.statusCode.value()
}
