package io.klaytn.finder.interfaces.rest.papi

import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.exception.NotFoundContractCodeException
import io.klaytn.finder.infra.exception.NotFoundContractException
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.papi.view.PapiContractItemView
import io.klaytn.finder.interfaces.rest.papi.view.mapper.PapiContractToItemViewMapper
import io.klaytn.finder.service.ContractImageService
import io.klaytn.finder.service.ContractService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.math.BigDecimal

@Profile(ServerMode.PRIVATE_API_MODE)
@RestController
@Tag(name= SwaggerConstant.TAG_PRIVATE)
class PapiContractController(
    val contractService: ContractService,
    val contractImageService: ContractImageService,
    val papiContractToItemViewMapper: PapiContractToItemViewMapper,
) {
    @Operation(
        description = "Retrieve contract information.",
        parameters = [
            Parameter(name = "contractAddress", description = "contract address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/papi/v1/contracts/{contractAddress}")
    fun getContract(@PathVariable contractAddress: String) =
        contractService.getContract(contractAddress)?.let { papiContractToItemViewMapper.transform(it) }
            ?: throw NotFoundContractException()

    @Operation(
        description = "Update contract information.",
    )
    @PutMapping(
        value = ["/papi/v1/contracts/{contractAddress}"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    fun updateContract(
        @PathVariable contractAddress: String,
        @RequestParam(required = false) contractType: ContractType?,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) symbol: String?,
        @RequestParam(required = false) decimal: Int?,
        @RequestParam(required = false) totalSupply: BigDecimal?,
        @RequestPart(required = false) tokenImage: MultipartFile?,
        @RequestParam(required = false) officialSite: String?,
        @RequestParam(required = false) officialEmailAddress: String?,
        @RequestParam(required = false) verified: Boolean?,
    ): PapiContractItemView {
        val contract = contractService.getContract(contractAddress) ?: throw NotFoundContractCodeException()
        val tokenIcon = contractImageService.checkAndGetImageHash(contractAddress, tokenImage)

        contractType?.let { contract.contractType = it }
        name?.let { contract.name = it }
        symbol?.let { contract.symbol = it }
        decimal?.let { contract.decimal = it }
        totalSupply?.let { contract.totalSupply = it }
        tokenIcon?.let { contract.icon = it }
        officialSite?.let { contract.officialSite = it }
        officialEmailAddress?.let { contract.officialEmailAddress = it }
        verified?.let { contract.verified = it }

        contractService.saveContract(contract)
        return papiContractToItemViewMapper.transform(contract)
    }
}
