package io.klaytn.finder.infra.exception

import io.klaytn.finder.infra.error.ApplicationErrorType

class InternalServerErrorException(throwable: Throwable) : ApplicationErrorException(errorType, throwable) {
    companion object {
        private val errorType = ApplicationErrorType.INTERNAL_SERVER_ERROR
    }
}
