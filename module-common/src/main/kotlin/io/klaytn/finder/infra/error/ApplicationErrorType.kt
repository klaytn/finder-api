package io.klaytn.finder.infra.error

import org.springframework.http.HttpStatus

enum class ApplicationErrorType(
        val title: String,
        val message: String,
        val statusCode: HttpStatus
) {
    // ---------------------------------------------------------------------------------------------------------------------------------------------------------
    // Common
    // ---------------------------------------------------------------------------------------------------------------------------------------------------------

    INVALID_REQUEST(
            "invalid request",
            "Requested parameter is not valid. ({0})",
            HttpStatus.BAD_REQUEST
    ),
    FORBIDDEN("access denied", "Please confirm your authority.", HttpStatus.FORBIDDEN),
    UNAUTHORIZED_ACCESS(
            "unauthorized access",
            "Please confirm your authority.",
            HttpStatus.UNAUTHORIZED
    ),
    NOT_FOUND_RESOURCE(
            "not found resource",
            "The requested resource does not exist.",
            HttpStatus.NOT_FOUND
    ),
    INTERNAL_SERVER_ERROR(
            "internal server error",
            "An error occurred inside the server.",
            HttpStatus.INTERNAL_SERVER_ERROR
    ),
    SERVICE_UNAVAILABLE(
            "temporary overloading or maintenance of the server",
            "The service is temporarily unavailable. Please try again in a moment.",
            HttpStatus.SERVICE_UNAVAILABLE
    ),
    MAINTENANCE(
            "maintenance of the server",
            "The service is temporarily unavailable. Please try again in a moment.",
            HttpStatus.SERVICE_UNAVAILABLE
    ),
    NOT_FOUND_URL("invalid request", "The url({0}) does not exists.", HttpStatus.NOT_FOUND),
    NOT_IMPLEMENTED(
            "not implemented",
            "The function is not yet supported.",
            HttpStatus.BAD_REQUEST
    ),
    NOT_FOUND_GATEWAY_ROUTE(
            "invalid request",
            "The url({0}) does not exists.",
            HttpStatus.NOT_FOUND
    ),

    // ---------------------------------------------------------------------------------------------------------------------------------------------------------
    // service
    // ---------------------------------------------------------------------------------------------------------------------------------------------------------

    NOT_FOUND_ACCOUNT("not found account", "Account does not exists.", HttpStatus.NOT_FOUND),
    NOT_FOUND_CONTRACT("not found contract", "Contract does not exists.", HttpStatus.NOT_FOUND),
    NOT_FOUND_CONTRACT_CODE(
            "not found contract code",
            "Contract Code does not exists.",
            HttpStatus.NOT_FOUND
    ),
    NOT_FOUND_CONTRACT_ABI(
            "not found contract abi",
            "Contract ABI does not exists.",
            HttpStatus.NOT_FOUND
    ),
    NOT_FOUND_TOKEN("not found token", "Token does not exists.", HttpStatus.NOT_FOUND),
    NOT_FOUND_NFT("not found nft", "Nft does not exists.", HttpStatus.NOT_FOUND),
    NOT_FOUND_NFT_ITEM(
            "not found nft token item",
            "Nft token item does not exists.",
            HttpStatus.NOT_FOUND
    ),
    NOT_FOUND_NFT_INVENTORY_CONTENT_FROM_S3(
            "not found nft inventory",
            "Nft inventory does not exists in s3.",
            HttpStatus.NOT_FOUND
    ),
    NOT_FOUND_BLOCK("not found block", "Block does not exists.", HttpStatus.NOT_FOUND),
    NOT_FOUND_BLOCK_BURNT(
            "not found block burnt",
            "Block burnt does not exists.",
            HttpStatus.NOT_FOUND
    ),
    NOT_FOUND_BLOCK_REWARD(
            "not found block reward",
            "Block reward does not exists.",
            HttpStatus.NOT_FOUND
    ),
    NOT_FOUND_TRANSACTION(
            "not found transaction",
            "Transaction does not exists.",
            HttpStatus.NOT_FOUND
    ),
    INVALID_CONTRACT_SUBMISSION("invalid contract submission", "{0}", HttpStatus.BAD_REQUEST),
    NFT_TOKEN_URI_REFRESH_REQUEST_LIMITED(
            "request limited",
            "Continuous Nft TokenURI refresh requests are limited.",
            HttpStatus.TOO_MANY_REQUESTS
    ),
    API_REQUEST_QUOTA_EXCEEDED(
            "request limited",
            "Request quota exceeded. (type:{0}, quotas:{1})",
            HttpStatus.TOO_MANY_REQUESTS
    )
}
