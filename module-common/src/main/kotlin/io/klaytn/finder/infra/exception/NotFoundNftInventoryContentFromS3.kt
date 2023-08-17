package io.klaytn.finder.infra.exception

import io.klaytn.finder.infra.error.ApplicationErrorType

class NotFoundNftInventoryContentFromS3 : ApplicationErrorException {
    constructor() : super(errorType)
    constructor(throwable: Throwable) : super(errorType, throwable)

    companion object {
        private val errorType = ApplicationErrorType.NOT_FOUND_NFT_INVENTORY_CONTENT_FROM_S3
    }
}