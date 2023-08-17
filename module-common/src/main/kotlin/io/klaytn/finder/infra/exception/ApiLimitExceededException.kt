package io.klaytn.finder.infra.exception

import io.klaytn.finder.infra.error.ApplicationErrorType

class ApiLimitExceededException() : ApplicationErrorException(errorType) {
    companion object {
        private val errorType = ApplicationErrorType.UNAUTHORIZED_ACCESS
    }
}
