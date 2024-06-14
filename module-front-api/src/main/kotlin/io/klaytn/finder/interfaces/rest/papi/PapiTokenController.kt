package io.klaytn.finder.interfaces.rest.papi

import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.exception.NotFoundTokenException
import io.klaytn.finder.infra.web.model.BlockRangeRequest
import io.klaytn.finder.infra.web.model.PapiSimplePageRequest
import io.klaytn.finder.infra.web.model.ScopePage
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.api.view.mapper.ContractToTokenItemViewMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.TokenHolderToListViewMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.TokenTransferToListViewMapper
import io.klaytn.finder.interfaces.rest.api.view.model.token.TokenTransferListView
import io.klaytn.finder.service.BlockRangeIntervalType
import io.klaytn.finder.service.BlockRangeService
import io.klaytn.finder.service.TokenService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Profile(ServerMode.PRIVATE_API_MODE)
@RestController
@Tag(name = SwaggerConstant.TAG_PRIVATE)
class PapiTokenController(
    val tokenService: TokenService,
    val blockRangeService: BlockRangeService,
    val contractToTokenItemViewMapper: ContractToTokenItemViewMapper,
    val tokenTransferToListViewMapper: TokenTransferToListViewMapper,
    val tokenHolderToListViewMapper: TokenHolderToListViewMapper,
) {
    @Operation(
        description = "Retrieve specific token information.",
        parameters = [
            Parameter(name = "tokenAddress", description = "token address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/papi/v1/tokens/{tokenAddress}")
    fun getToken(@PathVariable tokenAddress: String) =
        tokenService.getToken(tokenAddress)?.let { contractToTokenItemViewMapper.transform(it) }
            ?: throw NotFoundTokenException()

    @Operation(
        description = "Retrieve the list of holders for a specific token.",
        parameters = [
            Parameter(name = "tokenAddress", description = "token address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/papi/v1/tokens/{tokenAddress}/holders")
    fun getHolders(
        @PathVariable tokenAddress: String,
        @RequestParam(required = false) holderAddress: String?,
        @Valid papiSimplePageRequest: PapiSimplePageRequest
    ) =
        ScopePage.of(
            tokenService.getTokenHolders(tokenAddress, holderAddress, papiSimplePageRequest.toSimplePageRequest()),
            tokenHolderToListViewMapper
        )

    @Operation(
        description = "Retrieve the list of transfers for a specific token.",
        parameters = [
            Parameter(name = "tokenAddress", description = "token address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/papi/v1/tokens/{tokenAddress}/transfers")
    fun getTransfers(
        @PathVariable tokenAddress: String,
        @Valid blockRangeRequest: BlockRangeRequest? = null,
        @Valid papiSimplePageRequest: PapiSimplePageRequest
    ): ScopePage<TokenTransferListView> {
        val blockRangeIntervalType = BlockRangeIntervalType.TOKEN_TRANSFER
        val blockRange = blockRangeService.getBlockRange(blockRangeRequest, blockRangeIntervalType)

        return ScopePage.of(
            tokenService.getTokenTransfersByTokenAddress(
                tokenAddress, blockRange, papiSimplePageRequest.toSimplePageRequest()
            ),
            tokenTransferToListViewMapper,
            blockRangeService.getBlockRangeCondition(blockRange, blockRangeIntervalType)
        )
    }
}
