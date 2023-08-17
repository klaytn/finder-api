package io.klaytn.finder.infra.exception

import io.klaytn.finder.infra.error.ApplicationErrorType

class ApiRequestQuotaExceededException(vararg arguments: Any) :
    ApplicationErrorException(ApplicationErrorType.API_REQUEST_QUOTA_EXCEEDED, *arguments)