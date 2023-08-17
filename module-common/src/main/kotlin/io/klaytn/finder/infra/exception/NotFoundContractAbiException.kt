package io.klaytn.finder.infra.exception

import io.klaytn.finder.infra.error.ApplicationErrorType

class NotFoundContractAbiException() : ApplicationErrorException(errorType) {
    companion object {
        private val errorType = ApplicationErrorType.NOT_FOUND_CONTRACT_ABI
    }
}
