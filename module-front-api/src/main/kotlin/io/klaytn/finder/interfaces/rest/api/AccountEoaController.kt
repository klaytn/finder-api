package io.klaytn.finder.interfaces.rest.api

import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.domain.common.TransferType
import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.web.model.AccountTransferContractPageRequest
import io.klaytn.finder.infra.web.model.BlockRangeRequest
import io.klaytn.finder.infra.web.model.ScopePage
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.api.view.mapper.NftHolderToAccountNftBalanceListViewMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.NftTransferToListViewMapper
import io.klaytn.finder.interfaces.rest.api.view.model.nft.NftTransferListView
import io.klaytn.finder.service.AccountService
import io.klaytn.finder.service.AccountTransferContractService
import io.klaytn.finder.service.BlockRangeIntervalType
import io.klaytn.finder.service.BlockRangeService
import io.klaytn.finder.service.accountkey.AccountKeyService
import io.klaytn.finder.service.nft.NftService
import io.klaytn.finder.view.mapper.AccountKeyToListViewMapper
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
class AccountEoaController(
    val accountService: AccountService,
    val nftService: NftService,
    val blockRangeService: BlockRangeService,
    val accountTransferContractService: AccountTransferContractService,
    val accountKeyService: AccountKeyService,
    val nftTransferToListViewMapper: NftTransferToListViewMapper,
    val nftHolderToAccountNftBalanceListViewMapper: NftHolderToAccountNftBalanceListViewMapper,
    val accountKeyToListViewMapper: AccountKeyToListViewMapper
) {
    @Operation(
        description = "Retrieve the list of NFT transfers for the account.",
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
        description = "Retrieve the list of NFT transfer filters (contractAddress) for the account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}/nft-transfer-filters")
    fun getNftTransferFilters(
        @PathVariable accountAddress: String,
        @Valid accountTransferContractPageRequest: AccountTransferContractPageRequest,
    ) =
        accountTransferContractService.getAccountTransferContracts(
            accountService.checkAndGetAddress(accountAddress), TransferType.NFT, accountTransferContractPageRequest)

    @Operation(
        description = "Retrieve the list of NFT KIP-17 balances for the account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}/nft-kip17-balances")
    fun getNftKip17Balances(
        @PathVariable accountAddress: String,
        @Valid simplePageRequest: SimplePageRequest,
    ) = ScopePage.of(
        nftService.getNftBalancesOfHolder(
            ContractType.KIP17, accountService.checkAndGetAddress(accountAddress), simplePageRequest),
        nftHolderToAccountNftBalanceListViewMapper)

    @Operation(
        description = "Retrieve the list of NFT KIP-37 balances for the account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}/nft-kip37-balances")
    fun getNftKip37Balances(
        @PathVariable accountAddress: String,
        @Valid simplePageRequest: SimplePageRequest,
    ) = ScopePage.of(
        nftService.getNftBalancesOfHolder(
            ContractType.KIP37, accountService.checkAndGetAddress(accountAddress), simplePageRequest),
        nftHolderToAccountNftBalanceListViewMapper)

    @Operation(
        description = "List account keys created by the corresponding account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}/key-histories")
    fun getAccountKeys(
        @PathVariable accountAddress: String,
        @Valid simplePageRequest: SimplePageRequest
    ) =
        ScopePage.of(
            accountKeyService.getAccountKeysByAccountAddress(accountAddress, simplePageRequest),
            accountKeyToListViewMapper
        )
}
