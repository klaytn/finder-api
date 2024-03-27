package io.klaytn.finder.interfaces.rest.api

import io.klaytn.finder.infra.exception.NotFoundContractAbiException
import io.klaytn.finder.infra.exception.NotFoundContractCodeException
import io.klaytn.finder.infra.exception.NotFoundContractException
import io.klaytn.finder.infra.exception.NotImplementedException
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.api.view.mapper.ContractToItemViewMapper
import io.klaytn.finder.service.ContractCodeService
import io.klaytn.finder.service.ContractService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = SwaggerConstant.TAG_PUBLIC)
class ContractController(
    private val contractService: ContractService,
    private val contractCodeService: ContractCodeService,
    private val contractToItemViewMapper: ContractToItemViewMapper
) {
    @Operation(
        description = "Retrieve contract information.",
        parameters = [
            Parameter(name = "contractAddress", description = "contract address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/contracts/{contractAddress}")
    fun getContract(
        @PathVariable contractAddress: String
    ) = contractService.getContract(contractAddress)?.let { contractToItemViewMapper.transform(it) }
        ?: throw NotFoundContractException()


    @Operation(
        description = "Retrieve contract information.",
        parameters = [
            Parameter(name = "contractAddresses", description = "contract address list", `in` = ParameterIn.QUERY),
        ]
    )
    @GetMapping("/api/v1/contracts")
    fun getContracts(
        @RequestParam contractAddresses: List<String>
        ) = contractService.getContracts(contractAddresses).map { contractToItemViewMapper.transform(it) }

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
