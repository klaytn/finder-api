package io.klaytn.finder.infra.exception

import io.klaytn.finder.infra.error.ApplicationErrorType

class NotImplementedException() : ApplicationErrorException(errorType) {
    companion object {
        private val errorType = ApplicationErrorType.NOT_IMPLEMENTED
    }
}
