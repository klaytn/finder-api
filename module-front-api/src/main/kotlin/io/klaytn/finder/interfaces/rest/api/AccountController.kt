package io.klaytn.finder.interfaces.rest.api

import com.klaytn.caver.transaction.type.TransactionType
import io.klaytn.finder.domain.common.MyContractType
import io.klaytn.finder.domain.common.TransferType
import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.client.opensearch.AccountSearchType
import io.klaytn.finder.infra.web.model.AccountTransferContractPageRequest
import io.klaytn.finder.infra.web.model.BlockRangeRequest
import io.klaytn.finder.infra.web.model.ScopePage
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.api.view.mapper.*
import io.klaytn.finder.interfaces.rest.api.view.model.token.TokenTransferListView
import io.klaytn.finder.interfaces.rest.api.view.model.transaction.TransactionListView
import io.klaytn.finder.interfaces.rest.api.view.model.transaction.internal.InternalTransactionListView
import io.klaytn.finder.service.*
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
class AccountController(
    val accountService: AccountService,
    val transactionService: TransactionService,
    val internalTransactionService: InternalTransactionService,
    val tokenService: TokenService,
    val accountApproveService: AccountApproveService,
    val contractService: ContractService,
    val blockRangeService: BlockRangeService,
    val accountTransferContractService: AccountTransferContractService,
    val accountToItemViewMapper: AccountToItemViewMapper,
    val accountListViewMapper: AccountToAccountListViewMapper,
    val transactionToListViewMapper: TransactionToListViewMapper,
    val internalTransactionToListViewMapper: InternalTransactionToListViewMapper,
    val tokenTransferToListViewMapper: TokenTransferToListViewMapper,
    val tokenHolderToTokenBalanceListViewMapper: TokenHolderToTokenBalanceListViewMapper,
    val accountTokenApproveToListViewMapper: AccountTokenApproveToListViewMapper,
    val accountNftApproveToNftListViewMapper: AccountNftApproveToNftListViewMapper,
    val contractToListViewMapper: ContractToListViewMapper,
) {
    @Operation(
        description = "Retrieve specific account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH)
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}")
    fun getAccount(@PathVariable accountAddress: String) =
        accountService.getAccount(accountService.checkAndGetAddress(accountAddress)).let { account ->
            accountToItemViewMapper.transform(account)
        }

    @Operation(
        description = "Searches for accounts",
        parameters = [
            Parameter(name = "accountSearchType", description = "account Search Type", `in` = ParameterIn.PATH)
        ]
    )
    @GetMapping("/api/v1/accounts/searches/{accountSearchType}")
    fun searchAccounts(
        @PathVariable accountSearchType: AccountSearchType,
        @RequestParam keyword: String,
        @Valid simplePageRequest: SimplePageRequest) =
        ScopePage.of(
            accountService.search(accountSearchType, keyword, simplePageRequest),
            accountListViewMapper
        )

    @Operation(
        description = "Retrieve transactions of an account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH)
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}/transactions")
    fun getTransactions(
        @PathVariable accountAddress: String,
        @Valid blockRangeRequest: BlockRangeRequest? = null,
        @RequestParam(name = "type", required = false) type: TransactionType?,
        @Valid simplePageRequest: SimplePageRequest,
    ): ScopePage<TransactionListView> {
        val blockRangeIntervalType = BlockRangeIntervalType.TRANSACTION
        val blockRange = blockRangeService.getBlockRange(blockRangeRequest, blockRangeIntervalType)

        return ScopePage.of(
            transactionService.getTransactionsByAccountAddress(
                accountService.checkAndGetAddress(accountAddress), blockRange, type, simplePageRequest
            ),
            transactionToListViewMapper,
            blockRangeService.getBlockRangeCondition(blockRange, blockRangeIntervalType)
        )
    }

    @Operation(
        description = "Retrieve the list of internal transactions of an account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH)
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}/internal-transactions")
    fun getInternalTransactions(
        @PathVariable accountAddress: String,
        @Valid blockRangeRequest: BlockRangeRequest? = null,
        @Valid simplePageRequest: SimplePageRequest
    ): ScopePage<InternalTransactionListView> {
        val blockRangeIntervalType = BlockRangeIntervalType.INTERNAL_TRANSACTION
        val blockRange = blockRangeService.getBlockRange(blockRangeRequest, blockRangeIntervalType)

        return ScopePage.of(
            internalTransactionService.getInternalTransactionsByAccountAddress(
                accountService.checkAndGetAddress(accountAddress), blockRange, simplePageRequest
            ),
            internalTransactionToListViewMapper,
            blockRangeService.getBlockRangeCondition(blockRange, blockRangeIntervalType)
        )
    }

    @Operation(
        description = "Retrieve the list of transactions where the account has paid fees.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH)
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}/fee-paid-transactions")
    fun getFeePaidTransactions(
        @PathVariable accountAddress: String,
        @Valid blockRangeRequest: BlockRangeRequest? = null,
        @RequestParam(name = "type", required = false) type: TransactionType?,
        @Valid simplePageRequest: SimplePageRequest,
    ): ScopePage<TransactionListView> {
        val blockRangeIntervalType = BlockRangeIntervalType.TRANSACTION
        val blockRange = blockRangeService.getBlockRange(blockRangeRequest, blockRangeIntervalType)

        return ScopePage.of(
            transactionService.getTransactionsByFeePayer(
                accountService.checkAndGetAddress(accountAddress), blockRange, type, simplePageRequest),
            transactionToListViewMapper,
            blockRangeService.getBlockRangeCondition(blockRange, blockRangeIntervalType)
        )
    }


    @Operation(
        description = "Retrieve the list of token transfers for the account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
            Parameter(name = "contractAddress", description = "contract address", `in` = ParameterIn.QUERY),
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
        description = "Retrieve the list of token transfer filters (by contract address) for the account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}/token-transfer-filters")
    fun getTokenTransferFilters(
        @PathVariable accountAddress: String,
        @Valid accountTransferContractPageRequest: AccountTransferContractPageRequest,
    ) =
        accountTransferContractService.getAccountTransferContracts(
            accountService.checkAndGetAddress(accountAddress),
            TransferType.TOKEN,
            accountTransferContractPageRequest)

    @Operation(
        description = "Retrieve the list of token balances for the account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}/token-balances")
    fun getTokenBalances(@PathVariable accountAddress: String, @Valid simplePageRequest: SimplePageRequest) =
        ScopePage.of(
            tokenService.getTokenBalancesByHolder(
                accountService.checkAndGetAddress(accountAddress), simplePageRequest),
            tokenHolderToTokenBalanceListViewMapper
        )

    @Operation(
        description = "Approved tokens for the specific account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}/approved-tokens")
    fun getApprovedTokens(
        @PathVariable accountAddress: String,
        @RequestParam(name = "spenderAddress", required = false) spenderAddress: String?,
        @Valid simplePageRequest: SimplePageRequest
    ) =
        ScopePage.of(
            accountApproveService.getApporvedTokens(accountAddress, spenderAddress, simplePageRequest),
            accountTokenApproveToListViewMapper
        )

    @Operation(
        description = "Approved NFTs for the specific account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}/approved-nfts")
    fun getApprovedNfts(
        @PathVariable accountAddress: String,
        @RequestParam(name = "spenderAddress", required = false) spenderAddress: String?,
        @Valid simplePageRequest: SimplePageRequest
    ) =
        ScopePage.of(
            accountApproveService.getApprovedNfts(accountAddress, spenderAddress, true, simplePageRequest),
            accountNftApproveToNftListViewMapper
        )

    @Operation(
        description = "Approved NFTs by token ID for the specific account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}/approved-nft-tokenids")
    fun getApprovedNftTokenIds(
        @PathVariable accountAddress: String,
        @RequestParam(name = "spenderAddress", required = false) spenderAddress: String?,
        @Valid simplePageRequest: SimplePageRequest
    ) =
        ScopePage.of(
            accountApproveService.getApprovedNfts(accountAddress, spenderAddress, false, simplePageRequest),
            accountNftApproveToNftListViewMapper
        )

    @Operation(
        description = "List of contracts created by the specific account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}/contracts")
    fun getContractsByContractDeployerAddress(
        @PathVariable accountAddress: String,
        @RequestParam("type") type: MyContractType,
        @Valid simplePageRequest: SimplePageRequest
    ) =
        ScopePage.of(
            contractService.getContractsByContractDeployerAddress(
                accountAddress, type.contractTypes, simplePageRequest),
            contractToListViewMapper)
}
