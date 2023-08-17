package io.klaytn.finder.interfaces.rest.api

import io.klaytn.finder.infra.exception.NotFoundContractAbiException
import io.klaytn.finder.infra.exception.NotFoundContractCodeException
import io.klaytn.finder.infra.exception.NotImplementedException
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.service.ContractCodeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = SwaggerConstant.TAG_PUBLIC)
class ContractController(
    private val contractCodeService: ContractCodeService,
) {
    @Operation(
        description = "Retrieve the ABI (Application Binary Interface) of a contract.",
        parameters = [
            Parameter(name = "contractAddress", description = "contract address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/contracts/{contractAddress}/abi")
    fun getAbiOfContract(
        @PathVariable contractAddress: String
    ) = contractCodeService.getContractCode(contractAddress)?.contractAbi ?: throw NotFoundContractAbiException()
}
