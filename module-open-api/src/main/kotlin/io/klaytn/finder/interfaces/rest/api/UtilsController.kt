package io.klaytn.finder.interfaces.rest.api

import io.klaytn.commons.model.response.SimpleResponse
import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.domain.common.WalletType
import io.klaytn.finder.infra.exception.InvalidRequestException
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.service.caver.CaverAccountService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@Tag(name= SwaggerConstant.TAG_PUBLIC)
class UtilsController(
    private val caverAccountService: CaverAccountService
) {
    private val logger = logger(this::class.java)

    @Operation(
        description = "Verify signature information.",
        parameters = [
            Parameter(name = "walletType", description = "Type of wallet", `in` = ParameterIn.QUERY),
            Parameter(name = "address", description = "Wallet address that created the signature", `in` = ParameterIn.QUERY),
            Parameter(name = "message", description = "Message used to create the signature", `in` = ParameterIn.QUERY),
            Parameter(name = "signature", description = "Signature to be verified", `in` = ParameterIn.QUERY),
        ]
    )
    @PostMapping("/api/v1/utils/verify-signatures")
    fun verifyMessages(
        @RequestParam walletType: WalletType,
        @RequestParam address: String,
        @RequestParam message: String,
        @RequestParam signature: String
    ) = SimpleResponse(caverAccountService.verifySignature(walletType, address, message, signature))
}
