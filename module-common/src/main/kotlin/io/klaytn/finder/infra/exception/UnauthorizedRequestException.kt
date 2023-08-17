package io.klaytn.finder.infra.exception

import io.klaytn.finder.infra.error.ApplicationErrorType

class UnauthorizedRequestException(message: String) : ApplicationErrorException(errorType, message) {
    companion object {
        private val errorType = ApplicationErrorType.UNAUTHORIZED_ACCESS
    }
}
