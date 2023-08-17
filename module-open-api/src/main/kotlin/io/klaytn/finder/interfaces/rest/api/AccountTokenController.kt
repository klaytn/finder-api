package io.klaytn.finder.interfaces.rest.api

import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.infra.web.model.BlockRangeRequest
import io.klaytn.finder.infra.web.model.ScopePage
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.api.view.mapper.nft.NftHolderToAccountNftBalanceListViewMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.nft.NftTransferToListViewMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.token.TokenHolderToTokenBalanceListViewMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.token.TokenTransferToListViewMapper
import io.klaytn.finder.interfaces.rest.api.view.model.nft.NftTransferListView
import io.klaytn.finder.interfaces.rest.api.view.model.token.TokenTransferListView
import io.klaytn.finder.service.AccountService
import io.klaytn.finder.service.BlockRangeIntervalType
import io.klaytn.finder.service.BlockRangeService
import io.klaytn.finder.service.TokenService
import io.klaytn.finder.service.nft.NftService
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
class AccountTokenController(
    private val tokenService: TokenService,
    private val nftService: NftService,
    private val accountService: AccountService,
    private val blockRangeService: BlockRangeService,

    private val tokenTransferToListViewMapper: TokenTransferToListViewMapper,
    private val nftTransferToListViewMapper: NftTransferToListViewMapper,
    private val tokenHolderToTokenBalanceListViewMapper: TokenHolderToTokenBalanceListViewMapper,
    private val nftHolderToAccountNftBalanceListViewMapper: NftHolderToAccountNftBalanceListViewMapper,
) {
    // -- --------------------------------------------------------------------------------------------------------------
    // -- fts
    // -- --------------------------------------------------------------------------------------------------------------

    @Operation(
        description = "List of fungible token transfers related to the account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
            Parameter(name = "contractAddress", description = "contract address", `in` = ParameterIn.QUERY)
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}/token-transfers")
    fun getTokenTransfers(
        @PathVariable accountAddress: String,
        @RequestParam(name = "contractAddress", required = false) contractAddress: String? = null,
        @Valid blockRangeRequest: BlockRangeRequest? = null,
        @Valid simplePageRequest: SimplePageRequest,
    ): ScopePage<TokenTransferListView> {
        val blockRangeIntervalType = BlockRangeIntervalType.BLOCK
        val blockRange = blockRangeService.getBlockRange(blockRangeRequest, blockRangeIntervalType)

        return ScopePage.of(
            tokenService.getTokenTransfersByAccountAddress(
                accountService.checkAndGetAddress(accountAddress), contractAddress, blockRange, simplePageRequest),
            tokenTransferToListViewMapper,
            blockRangeService.getBlockRangeCondition(blockRange, blockRangeIntervalType)
        )
    }

    @Operation(
        description = "List of fungible token balances owned by the account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}/token-balances")
    fun getAccountFungibleTokenBalances(
        @PathVariable accountAddress: String,
        @Valid simplePageRequest: SimplePageRequest
    ) = ScopePage.of(
        tokenService.getTokenBalancesByHolder(
            accountService.checkAndGetAddress(accountAddress), simplePageRequest),
        tokenHolderToTokenBalanceListViewMapper)

    // -- --------------------------------------------------------------------------------------------------------------
    // -- nfts
    // -- --------------------------------------------------------------------------------------------------------------

    @Operation(
        description = "List of non-fungible token transfers related to the account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
            Parameter(name = "contractAddress", description = "contract address", `in` = ParameterIn.QUERY)
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}/nft-transfers")
    fun getNftTransfers(
        @PathVariable accountAddress: String,
        @RequestParam(name = "contractAddress", required = false) contractAddress: String? = null,
        @Valid blockRangeRequest: BlockRangeRequest? = null,
        @Valid simplePageRequest: SimplePageRequest,
    ): ScopePage<NftTransferListView> {
        val blockRangeIntervalType = BlockRangeIntervalType.NFT_TRANSFER
        val blockRange = blockRangeService.getBlockRange(blockRangeRequest, blockRangeIntervalType)

        return ScopePage.of(
            nftService.getNftTransfersByAccountAddress(
                accountService.checkAndGetAddress(accountAddress), contractAddress, blockRange, simplePageRequest),
            nftTransferToListViewMapper,
            blockRangeService.getBlockRangeCondition(blockRange, blockRangeIntervalType)
        )
    }

    @Operation(
        description = "List of non-fungible token (KIP-17) balances owned by the account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}/nft-balances/kip17")
    fun getAccountNonFungibleTokenBalancesOfKip17(
        @PathVariable accountAddress: String,
        @Valid simplePageRequest: SimplePageRequest,
    ) = ScopePage.of(
        nftService.getNftBalancesOfHolder(
            ContractType.KIP17, accountService.checkAndGetAddress(accountAddress), simplePageRequest),
        nftHolderToAccountNftBalanceListViewMapper)

    @Operation(
        description = "List of non-fungible token (KIP-37) balances owned by the account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}/nft-balances/kip37")
    fun getAccountNonFungibleTokenBalancesOfKip37(
        @PathVariable accountAddress: String,
        @Valid simplePageRequest: SimplePageRequest,
    ) = ScopePage.of(
        nftService.getNftBalancesOfHolder(
            ContractType.KIP37, accountService.checkAndGetAddress(accountAddress), simplePageRequest),
        nftHolderToAccountNftBalanceListViewMapper)
}
