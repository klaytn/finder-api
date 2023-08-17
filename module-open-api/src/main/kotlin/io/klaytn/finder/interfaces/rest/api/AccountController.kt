package io.klaytn.finder.interfaces.rest.api

import com.klaytn.caver.transaction.type.TransactionType
import io.klaytn.finder.infra.web.model.BlockRangeRequest
import io.klaytn.finder.infra.web.model.ScopePage
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.api.view.mapper.AccountToViewMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.EventLogToListViewMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.transaction.TransactionToListViewMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.transaction.internal.InternalTransactionToListViewMapper
import io.klaytn.finder.interfaces.rest.api.view.model.EventLogListView
import io.klaytn.finder.interfaces.rest.api.view.model.transaction.TransactionListView
import io.klaytn.finder.interfaces.rest.api.view.model.transaction.internal.InternalTransactionListView
import io.klaytn.finder.service.*
import io.klaytn.finder.service.accountkey.AccountKeyService
import io.klaytn.finder.view.mapper.AccountKeyToListViewMapper
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
class AccountController(
    private val accountService: AccountService,
    private val transactionService: TransactionService,
    private val internalTransactionService: InternalTransactionService,
    private val eventLogService: EventLogService,
    private val blockRangeService: BlockRangeService,
    private val accountKeyService: AccountKeyService,
    private val accountToViewMapper: AccountToViewMapper,
    private val transactionToListViewMapper: TransactionToListViewMapper,
    private val internalTransactionToListViewMapper: InternalTransactionToListViewMapper,
    private val eventLogToListViewMapper: EventLogToListViewMapper,
    private val accountKeyToListViewMapper: AccountKeyToListViewMapper
) {
    @Operation(
        description = "Retrieve an account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH)
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}")
    fun getAccount(
        @PathVariable accountAddress: String
    ) =
        accountService.getAccount(accountService.checkAndGetAddress(accountAddress)).let { account ->
            accountToViewMapper.transform(account)
        }

    @Operation(
        description = "Retrieve a list of transactions for an account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH)
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}/transactions")
    fun getAccountTransactions(
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
        description = "Retrieve a list of internal transactions for an account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH)
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}/internal-transactions")
    fun getAccountInternalTransactions(
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
        description = "Retrieve a list of event logs associated with an account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
            Parameter(name = "signature", description = "signature hash", `in` = ParameterIn.QUERY),
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}/event-logs")
    fun getEventLogs(
        @PathVariable accountAddress: String,
        @RequestParam(required = false) signature: String?,
        @Valid blockRangeRequest: BlockRangeRequest? = null,
        @Valid simplePageRequest: SimplePageRequest,
    ): ScopePage<EventLogListView> {
        val blockRangeIntervalType = BlockRangeIntervalType.EVENT_LOG
        val blockRange = blockRangeService.getBlockRange(blockRangeRequest, blockRangeIntervalType)

        return ScopePage.of(
            eventLogService.getEventLogsByAccountAddress(
                accountService.checkAndGetAddress(accountAddress), signature, blockRange, simplePageRequest),
            eventLogToListViewMapper,
            blockRangeService.getBlockRangeCondition(blockRange, blockRangeIntervalType)
        )
    }

    @Operation(
        description = "List of account keys generated by the given account.",
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
