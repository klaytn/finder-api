package io.klaytn.finder.infra.exception

import io.klaytn.finder.infra.error.ApplicationErrorType

class InvalidRequestException : ApplicationErrorException {
    constructor() : super(errorType)
    constructor(vararg arguments: Any) : super(errorType, *arguments)
    constructor(throwable: Throwable) : super(errorType, throwable)

    companion object {
        private val errorType = ApplicationErrorType.INVALID_REQUEST
    }
}
