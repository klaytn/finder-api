package io.klaytn.finder.interfaces.rest.api

import io.klaytn.finder.infra.exception.NotFoundTokenException
import io.klaytn.finder.infra.web.model.BlockRangeRequest
import io.klaytn.finder.infra.web.model.ScopePage
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.api.view.mapper.token.*
import io.klaytn.finder.interfaces.rest.api.view.model.token.TokenBurnListView
import io.klaytn.finder.interfaces.rest.api.view.model.token.TokenTransferListView
import io.klaytn.finder.service.BlockRangeIntervalType
import io.klaytn.finder.service.BlockRangeService
import io.klaytn.finder.service.TokenService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@Tag(name = SwaggerConstant.TAG_PUBLIC)
class FungibleTokenController(
    private val tokenService: TokenService,
    private val blockRangeService: BlockRangeService,
    private val contractToTokenViewMapper: ContractToTokenViewMapper,
    private val tokenTransferToListViewMapper: TokenTransferToListViewMapper,
    private val tokenHolderToListViewMapper: TokenHolderToListViewMapper,
    private val tokenBurnToListViewMapper: TokenBurnToListViewMapper,
) {
    @Operation(
        description = "Retrieve information about a fungible token.",
        parameters = [
            Parameter(name = "tokenAddress", description = "token address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/tokens/{tokenAddress}")
    fun getFungibleToken(
        @PathVariable tokenAddress: String,
    ) =
        tokenService.getToken(tokenAddress)?.let { contractToTokenViewMapper.transform(it) }
            ?: throw NotFoundTokenException()

    @Operation(
        description = "Retrieve a list of holders for a fungible token.",
        parameters = [
            Parameter(name = "tokenAddress", description = "token address", `in` = ParameterIn.PATH),
            Parameter(name = "holderAddress", description = "holderAddress", `in` = ParameterIn.QUERY),
        ]
    )
    @GetMapping("/api/v1/tokens/{tokenAddress}/holders")
    fun getHoldersOfFungibleToken(
        @PathVariable tokenAddress: String,
        @RequestParam(required = false) holderAddress: String?,
        @Valid simplePageRequest: SimplePageRequest
    ) = ScopePage.of(
        tokenService.getTokenHolders(tokenAddress, holderAddress, simplePageRequest),
        tokenHolderToListViewMapper
    )

    @Operation(
        description = "Retrieve a list of transfers for a fungible token.",
        parameters = [
            Parameter(name = "tokenAddress", description = "token address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/tokens/{tokenAddress}/transfers")
    fun getTransfersOfFungibleToken(
        @PathVariable tokenAddress: String,
        @Valid blockRangeRequest: BlockRangeRequest? = null,
        @Valid simplePageRequest: SimplePageRequest
    ): ScopePage<TokenTransferListView> {
        val blockRangeIntervalType = BlockRangeIntervalType.TOKEN_TRANSFER
        val blockRange = blockRangeService.getBlockRange(blockRangeRequest, blockRangeIntervalType)

        return ScopePage.of(
            tokenService.getTokenTransfersByTokenAddress(
                tokenAddress, blockRangeRequest?.toLongRange(), simplePageRequest),
            tokenTransferToListViewMapper,
            blockRangeService.getBlockRangeCondition(blockRange, blockRangeIntervalType)
        )
    }

    @Operation(
        description = "Retrieve a list of burn events for a fungible token.",
        parameters = [
            Parameter(name = "tokenAddress", description = "token address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/tokens/{tokenAddress}/burns")
    fun getBurnsOfFungibleToken(
        @PathVariable tokenAddress: String,
        @Valid blockRangeRequest: BlockRangeRequest? = null,
        @Valid simplePageRequest: SimplePageRequest
    ): ScopePage<TokenBurnListView> {
        val blockRangeIntervalType = BlockRangeIntervalType.TOKEN_BURN
        val blockRange = blockRangeService.getBlockRange(blockRangeRequest, blockRangeIntervalType)

        return ScopePage.of(
            tokenService.getTokenBurnsByTokenAddress(tokenAddress, blockRange, simplePageRequest),
            tokenBurnToListViewMapper,
            blockRangeService.getBlockRangeCondition(blockRange, blockRangeIntervalType)
        )
    }
}
