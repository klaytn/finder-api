package io.klaytn.finder.infra.exception

import io.klaytn.finder.infra.error.ApplicationErrorType

class NftTokenUriRefreshRequestLimitedException() : ApplicationErrorException(errorType) {
    companion object {
        private val errorType = ApplicationErrorType.NFT_TOKEN_URI_REFRESH_REQUEST_LIMITED
    }
}
