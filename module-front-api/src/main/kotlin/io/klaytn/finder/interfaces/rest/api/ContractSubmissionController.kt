package io.klaytn.finder.interfaces.rest.api

import io.klaytn.commons.model.response.SimpleResponse
import io.klaytn.commons.utils.retrofit2.orElseThrow
import io.klaytn.finder.domain.common.WalletType
import io.klaytn.finder.domain.mysql.set1.ContractSubmissionRequest
import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.client.ContractCompilerClient
import io.klaytn.finder.infra.client.OptionResult
import io.klaytn.finder.infra.exception.InvalidContractSubmissionException
import io.klaytn.finder.infra.utils.KlayUtils
import io.klaytn.finder.infra.utils.ValidatorUtils
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.service.ContractImageService
import io.klaytn.finder.service.ContractSubmissionService
import io.klaytn.finder.service.caver.CaverContractService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Profile(ServerMode.API_MODE)
@RestController
@Tag(name= SwaggerConstant.TAG_PUBLIC)
class ContractSubmissionController(
    val contractSubmissionService: ContractSubmissionService,
    val contractCompilerClient: ContractCompilerClient,
    val caverContractService: CaverContractService,
    val contractImageService: ContractImageService
) {
    @Operation(
        description = "Returns a list of options required for registering Contract source.",
    )
    @GetMapping(path = ["/api/v1/contract-submissions/options", "/api/v1/contract-codes"])
    fun getOptions(): ContractSubmissionOptionResponse {
        val options = contractCompilerClient.getOptions().orElseThrow { IllegalStateException() }
        return ContractSubmissionOptionResponse(
            licenses = options.licenses,
            versions = options.solidityVersions,
            evmVersions = options.evmVersions
        )
    }

    @Operation(
        description = "Registers Contract metadata and source code.",
    )
    @PostMapping(
        value = ["/api/v1/contract-submissions"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun registerContractCode(
        @RequestParam(required = false) walletType: WalletType?,
        @RequestParam(required = true) contractAddress: String,                 // for contract search
        @RequestParam(required = true) contractCreatorSignature: String,        // for extract creator address

        @RequestPart(required = true) sourceCode: MultipartFile,                // ContractCode#contractSourceCode
        @RequestParam(required = false) abiEncodedValue: String?,               // ABI-encoded constructor arguments or Raw ABI Code(truncated)

        @RequestParam(required = true) compilerVersion: String,                 // ContractCode#compilerVersion
        @RequestParam(required = true) licenseType: String,                     // ContractCode#licenseType
        @RequestParam(required = false) evmVersion: String?,                    // ContractCode#optimizationEvmVersion
        @RequestParam(required = true) optimization: Boolean,                   // ContractCode#optimizationFlag
        @RequestParam(required = false) optimizationRuns: Long?,                // ContractCode#optimizationRunsCount
        @RequestParam(required = false) libraries: List<String>?,

        @RequestParam(required = false) officialWebSite: String?,               // Contract#officialSite
        @RequestParam(required = false) officialEmailAddress: String?,          // Contract#officialEmailAddress

        @RequestPart(required = false) tokenImage: MultipartFile?,              // Contract#icon
    ): SimpleResponse<Boolean> {
        val tokenIcon = contractImageService.checkAndGetImageHash(contractAddress, tokenImage)

        officialEmailAddress?.let {
            if (!ValidatorUtils.isValidEmail(it)) {
                throw InvalidContractSubmissionException("invalid email address. please check email address.")
            }
        }

        officialWebSite?.let {
            if (!ValidatorUtils.isValidWebSiteAddress(it)) {
                throw InvalidContractSubmissionException(
                    "invalid web site address. please check web site address. (only support https)"
                )
            }
        }

        val contractSourceCode = String(sourceCode.bytes)
        val contractCreationCode = KlayUtils.stripHexPrefix(caverContractService.getContractCode(contractAddress))
        val contractSubmissionRequest = ContractSubmissionRequest(
            contractAddress = contractAddress,
            contractCreatorSignature = contractCreatorSignature,
            contractSourceCode = contractSourceCode,
            constructorArguments = abiEncodedValue,
            compilerVersion = compilerVersion,
            licenseType = licenseType,
            optimizationRuns = optimizationRuns,
            optimization = optimization,
            evmVersion = if ("default".equals(evmVersion, true)) null else evmVersion,
            tokenName = null,
            tokenSymbol = null,
            tokenIcon = tokenIcon,
            officialWebSite = officialWebSite,
            officialEmailAddress = officialEmailAddress,
            contractCreationCode = contractCreationCode,
            walletType = walletType,
            libraries = libraries
        )

        try {
            val compileResult =
                contractSubmissionService.checkAndCompile(contractSubmissionRequest)
            val caverContractCode = caverContractService.getContractCode(contractAddress)
            contractSubmissionService.submission(contractSubmissionRequest, caverContractCode, compileResult)

            contractSubmissionRequest.result = true
        } catch (exception: Exception) {
            val resultMessage =
                if (exception is InvalidContractSubmissionException && exception.messageArguments.isNotEmpty()) {
                    exception.messageArguments[0].toString()
                } else {
                    exception.message
                }

            contractSubmissionRequest.result = false
            contractSubmissionRequest.resultMessage = resultMessage

            throw exception
        } finally {
            contractSubmissionService.registerSubmissionRequest(contractSubmissionRequest)
        }
        return SimpleResponse(true)
    }
}

@Schema
data class ContractSubmissionOptionResponse(
    @Schema(title = "Licenses")
    val licenses: List<String>,

    @Schema(title = "Solidity Version")
    val versions: List<String>,

    @Schema(title = "EVM Version")
    val evmVersions: List<OptionResult.EvmVersion>,
)