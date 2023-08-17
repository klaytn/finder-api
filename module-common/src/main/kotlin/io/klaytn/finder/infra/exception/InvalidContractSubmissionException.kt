package io.klaytn.finder.infra.exception

import io.klaytn.finder.infra.error.ApplicationErrorType

class InvalidContractSubmissionException(vararg arguments: Any) :
    ApplicationErrorException(ApplicationErrorType.INVALID_CONTRACT_SUBMISSION, *arguments)