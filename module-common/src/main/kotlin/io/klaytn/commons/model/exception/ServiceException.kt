package io.klaytn.commons.model.exception

abstract class ServiceException(
        override val message: String?,
        override val cause: Throwable? = null
) : RuntimeException(message, cause) {
    abstract fun getCode(): String
    abstract fun getTitle(): String
    abstract fun getHttpStatus(): Int
}
