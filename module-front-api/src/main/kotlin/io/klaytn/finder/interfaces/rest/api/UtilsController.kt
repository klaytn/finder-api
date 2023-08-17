package io.klaytn.finder.interfaces.rest.api

import io.klaytn.commons.model.response.SimpleResponse
import io.klaytn.finder.domain.common.WalletType
import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.service.caver.CaverAccountService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*

@Profile(ServerMode.API_MODE)
@RestController
@Tag(name= SwaggerConstant.TAG_PUBLIC)
class UtilsController(
    private val caverAccountService: CaverAccountService
) {
    @Operation(
        description = "Verifies signature information.",
        parameters = [
            Parameter(name = "walletType", description = "wallet type", `in` = ParameterIn.QUERY),
            Parameter(name = "address", description =  "wallet address that generated the signature",  `in` = ParameterIn.QUERY),
            Parameter(name = "message", description =  "message used to generate the signature",  `in` = ParameterIn.QUERY),
            Parameter(name = "signature", description = "signature information to be verified",  `in` = ParameterIn.QUERY),
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