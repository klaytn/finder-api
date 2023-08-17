package io.klaytn.finder.infra.exception

import io.klaytn.finder.infra.error.ApplicationErrorType

class MaintenanceException(vararg arguments: Any) :
    ApplicationErrorException(ApplicationErrorType.MAINTENANCE, *arguments)