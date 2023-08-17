package io.klaytn.finder.interfaces.rest.api

import io.klaytn.finder.domain.common.WalletType
import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.exception.*
import io.klaytn.finder.infra.utils.ValidatorUtils
import io.klaytn.finder.infra.web.model.ScopePage
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.api.view.model.contract.ContractItemView
import io.klaytn.finder.interfaces.rest.api.view.mapper.ContractToItemViewMapper
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
import javax.validation.Valid

@Profile(ServerMode.API_MODE)
@RestController
@Tag(name = SwaggerConstant.TAG_PUBLIC)
class ContractController(
    private val contractService: ContractService,
    private val contractImageService: ContractImageService,
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
        description = "Retrieve a list of proxy contracts using the contract.",
        parameters = [
            Parameter(name = "contractAddress", description = "contract address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/contracts/{contractAddress}/proxies")
    fun getContractProxies(
        @PathVariable contractAddress: String,
        @Valid simplePageRequest: SimplePageRequest
    ) =
        ScopePage.of(
            contractService.getContractsByImplementationAddress(contractAddress, simplePageRequest),
            contractToItemViewMapper
        )

    @Operation(
        description = "Return the implementation contract being used by the contract.",
        parameters = [
            Parameter(name = "contractAddress", description = "contract address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/contracts/{contractAddress}/implementations")
    fun getContractImplementation(
        @PathVariable contractAddress: String,
    ) = contractService.getContract(contractAddress)?.let { contractToItemViewMapper.transform(it) }
        ?: NotFoundContractException()

    @PutMapping(
        value = ["/api/v1/contracts/{contractAddress}"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    fun updateContract(
        @PathVariable contractAddress: String,
        @RequestParam walletType: WalletType,
        @RequestParam contractCreatorSignature: String,
        @RequestParam(required = false) officialSite: String?,
        @RequestParam(required = false) officialEmailAddress: String?,
        @RequestPart(required = false) icon: MultipartFile?,
        @RequestParam(required = false) deleteIcon: Boolean = false,
    ): ContractItemView {
        checkSignature(contractAddress, walletType, contractCreatorSignature)

        officialSite?.let {
            if (!ValidatorUtils.isValidWebSiteAddress(it)) {
                throw InvalidRequestException(
                    "invalid web site address. please check web site address. (only support https)"
                )
            }
        }

        officialEmailAddress?.let {
            if (!ValidatorUtils.isValidEmail(it)) {
                throw InvalidRequestException("invalid email address. please check email address.")
            }
        }

        val uploadedIcon = contractImageService.checkAndGetImageHash(contractAddress, icon)
        contractService.getContract(contractAddress)?.let { contract ->
            contract.officialSite = if(!officialSite.isNullOrBlank()) officialSite else null
            contract.officialEmailAddress = if(!officialEmailAddress.isNullOrBlank()) officialEmailAddress else null
            if(!uploadedIcon.isNullOrBlank()) {
                contract.icon = uploadedIcon
            } else if(deleteIcon){
                contract.icon = null
            }

            contractService.saveContract(contract)
            return getContract(contractAddress)
        } ?: throw NotFoundContractException()
    }

    private fun checkSignature(contractAddress: String, walletType: WalletType, contractCreatorSignature: String) {
        if (!contractService.verifyContractOwner(contractAddress, walletType, contractCreatorSignature)) {
            throw InvalidRequestException("not contract owner. please check your wallet address.")
        }
    }
}
