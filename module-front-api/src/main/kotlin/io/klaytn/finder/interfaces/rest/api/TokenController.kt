package io.klaytn.finder.interfaces.rest.api

import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.exception.NotFoundTokenException
import io.klaytn.finder.infra.web.model.BlockRangeRequest
import io.klaytn.finder.infra.web.model.ScopePage
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.api.view.mapper.*
import io.klaytn.finder.interfaces.rest.api.view.model.token.TokenBurnListView
import io.klaytn.finder.interfaces.rest.api.view.model.token.TokenTransferListView
import io.klaytn.finder.service.BlockRangeIntervalType
import io.klaytn.finder.service.BlockRangeService
import io.klaytn.finder.service.FinderHomeService
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

@Profile(ServerMode.API_MODE)
@RestController
@Tag(name= SwaggerConstant.TAG_PUBLIC)
class TokenController(
    val tokenService: TokenService,
    val blockRangeService: BlockRangeService,
    val contractToTokenItemViewMapper: ContractToTokenItemViewMapper,
    val tokenTransferToListViewMapper: TokenTransferToListViewMapper,
    val tokenHolderToListViewMapper: TokenHolderToListViewMapper,
    val tokenBurnToListViewMapper: TokenBurnToListViewMapper,
    val finderHomeService: FinderHomeService,
    val contractToTokenListWithPriceInfoViewMapper: ContractToTokenListWithPriceInfoViewMapper
) {
    @Operation(
        description = "Retrieve the list of tokens.",
    )
    @GetMapping("/api/v1/tokens")
    fun getTokens(@Valid simplePageRequest: SimplePageRequest) =
        ScopePage.of(tokenService.getVerifiedTokens(simplePageRequest), contractToTokenListWithPriceInfoViewMapper)

    @Operation(
        description = "Retrieve specific token information.",
        parameters = [
            Parameter(name = "tokenAddress", description = "token address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/tokens/{tokenAddress}")
    fun getToken(@PathVariable tokenAddress: String) =
        tokenService.getToken(tokenAddress)?.let { contractToTokenItemViewMapper.transform(it) }
            ?: throw NotFoundTokenException()

    @Operation(
        description = "Retrieve a list of token holders.",
        parameters = [
            Parameter(name = "tokenAddress", description = "token address", `in` = ParameterIn.PATH),
            Parameter(name = "holderAddress", description = "holderAddress", `in` = ParameterIn.QUERY),
        ]
    )
    @GetMapping("/api/v1/tokens/{tokenAddress}/holders")
    fun getHolders(
        @PathVariable tokenAddress: String,
        @RequestParam(required = false) holderAddress: String?,
        @Valid simplePageRequest: SimplePageRequest
    ) = ScopePage.of(
        tokenService.getTokenHolders(tokenAddress, holderAddress, simplePageRequest),
        tokenHolderToListViewMapper
    )

    @Operation(
        description = "Retrieve a list of token transfers.",
        parameters = [
            Parameter(name = "tokenAddress", description = "token address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/tokens/{tokenAddress}/transfers")
    fun getTransfers(
        @PathVariable tokenAddress: String,
        @Valid blockRangeRequest: BlockRangeRequest? = null,
        @Valid simplePageRequest: SimplePageRequest
    ): ScopePage<TokenTransferListView> {
        val blockRangeIntervalType = BlockRangeIntervalType.TOKEN_TRANSFER
        val blockRange = blockRangeService.getBlockRange(blockRangeRequest, blockRangeIntervalType)

        return ScopePage.of(
            tokenService.getTokenTransfersByTokenAddress(tokenAddress, blockRange, simplePageRequest),
            tokenTransferToListViewMapper,
            blockRangeService.getBlockRangeCondition(blockRange, blockRangeIntervalType)
        )
    }

    @Operation(
        description = "Retrieve a list of token burns.",
        parameters = [
            Parameter(name = "tokenAddress", description = "token address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/tokens/{tokenAddress}/burns")
    fun getBurns(
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

    @Operation(
        description = "Returns the price of Klay.",
    )
    @GetMapping("/api/v1/tokens/klay/price")
    fun getKlayPrice() = finderHomeService.getKlayPrice()
}
