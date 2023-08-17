package io.klaytn.finder.interfaces.rest.api

import com.klaytn.caver.transaction.type.TransactionType
import io.klaytn.finder.infra.client.opensearch.model.TransactionSearchPageRequest
import io.klaytn.finder.infra.client.opensearch.model.TransactionSearchRequest
import io.klaytn.finder.infra.exception.NotFoundTransactionException
import io.klaytn.finder.infra.web.model.BlockRangeRequest
import io.klaytn.finder.infra.web.model.ScopePage
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.api.view.mapper.EventLogToListViewMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.nft.NftTransferToListViewMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.token.TokenTransferToListViewMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.transaction.TransactionToListViewMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.transaction.status.TransactionReceiptToStatusViewMapper
import io.klaytn.finder.view.mapper.transaction.status.TransactionToStatusViewMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.transaction.TransactionToViewMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.transaction.input.TransactionToInputDataViewMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.transaction.internal.InternalTransactionToLeveledListViewMapper
import io.klaytn.finder.interfaces.rest.api.view.model.transaction.TransactionListView
import io.klaytn.finder.service.*
import io.klaytn.finder.service.caver.CaverTransactionService
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
class TransactionController(
    private val caverTransactionService: CaverTransactionService,
    private val transactionService: TransactionService,
    private val internalTransactionService: InternalTransactionService,
    private val eventLogService: EventLogService,
    private val blockRangeService: BlockRangeService,
    private val tokenService: TokenService,
    private val nftService: NftService,
    private val transactionToViewMapper: TransactionToViewMapper,
    private val transactionToListViewMapper: TransactionToListViewMapper,
    private val transactionToStatusViewMapper: TransactionToStatusViewMapper,
    private val transactionReceiptToStatusViewMapper: TransactionReceiptToStatusViewMapper,
    private val transactionToInputDataViewMapper: TransactionToInputDataViewMapper,
    private val internalTransactionToLeveledListViewMapper: InternalTransactionToLeveledListViewMapper,
    private val eventLogToListViewMapper: EventLogToListViewMapper,
    private val tokenTransferToListViewMapper: TokenTransferToListViewMapper,
    private val nftTransferToListViewMapper: NftTransferToListViewMapper,
) {
    // -- --------------------------------------------------------------------------------------------------------------
    // -- transaction-receipts
    // -- --------------------------------------------------------------------------------------------------------------

    @Operation(
        description = "Retrieve the status of a transaction-receipt.",
        parameters = [
            Parameter(name = "transactionHash", description = "transaction hash", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/transaction-receipts/{transactionHash}/status")
    fun getTransactionReceiptStatus(
        @PathVariable transactionHash: String
    ) =
        caverTransactionService.getTransactionReceipt(transactionHash)?.let {
            transactionReceiptToStatusViewMapper.transform(it) } ?: throw NotFoundTransactionException()

    // -- --------------------------------------------------------------------------------------------------------------
    // -- transactions
    // -- --------------------------------------------------------------------------------------------------------------

    @Operation(
        description = "Retrieve a list of transactions.",
        parameters = [
            Parameter(name = "type", description = "transaction type", `in` = ParameterIn.QUERY),
        ]
    )
    @GetMapping("/api/v1/transactions")
    fun getTransactions(
        @RequestParam(name = "type", required = false) type: TransactionType?,
        @Valid blockRangeRequest: BlockRangeRequest?,
        @Valid simplePageRequest: SimplePageRequest,
    ): ScopePage<TransactionListView> {
        val blockRangeIntervalType = BlockRangeIntervalType.TRANSACTION
        val blockRange = blockRangeService.getBlockRange(blockRangeRequest, blockRangeIntervalType)

        return ScopePage.of(
            transactionService.getTransactions(blockRange, type, simplePageRequest),
            transactionToListViewMapper,
            blockRangeService.getBlockRangeCondition(blockRange, blockRangeIntervalType)
        )
    }

    @Operation(
        description = "Search for a transaction.",
    )
    @GetMapping("/api/v1/transactions/searches")
    fun searchTransactions(
        @Valid transactionSearchRequest: TransactionSearchRequest,
        @Valid transactionSearchPageRequest: TransactionSearchPageRequest
    ) =
        ScopePage.of(
            transactionService.search(transactionSearchRequest, transactionSearchPageRequest),
            transactionToListViewMapper)

    @Operation(
        description = "Retrieve a transaction.",
        parameters = [
            Parameter(name = "transactionHash", description = "transaction hash", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/transactions/{transactionHash}")
    fun getTransaction(
        @PathVariable transactionHash: String
    ) =
        transactionService.getTransactionByHash(transactionHash)?.let { transactionToViewMapper.transform(it) }
            ?: throw NotFoundTransactionException()

    @Operation(
        description = "Retrieve the status of a transaction.",
        parameters = [
            Parameter(name = "transactionHash", description = "transaction hash", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/transactions/{transactionHash}/status")
    fun getTransactionStatus(
        @PathVariable transactionHash: String
    ) =
        transactionService.getTransactionByHash(transactionHash)?.let {
            transactionToStatusViewMapper.transform(it) } ?: throw NotFoundTransactionException()

    @Operation(
        description = "Retrieve the input data of a transaction.",
        parameters = [
            Parameter(name = "transactionHash", description = "transaction hash", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/transactions/{transactionHash}/input-data")
    fun getTransactionInputData(
        @PathVariable transactionHash: String
    ) =
        transactionService.getTransactionByHash(transactionHash)?.let { transaction ->
            transaction.input?.let {
                transactionToInputDataViewMapper.transform(transaction)
            }
        }

    @Operation(
        description = "Retrieve the list of internal transactions for a specific transaction.",
        parameters = [
            Parameter(name = "transactionHash", description = "transaction hash", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/transactions/{transactionHash}/internal-transactions")
    fun getInternalTransactionsOfTransaction(
        @PathVariable transactionHash: String,
        @Valid simplePageRequest: SimplePageRequest
    ) =
        transactionService.getTransactionByHash(transactionHash)?.let {
            ScopePage.of(
                internalTransactionService.getInternalTransactionsByBlockNumberAndIndex(
                    it.blockNumber,
                    it.transactionIndex,
                    simplePageRequest
                ), internalTransactionToLeveledListViewMapper
            )
        } ?: throw NotFoundTransactionException()

    @Operation(
        description = "Retrieve the list of event logs for a specific transaction.",
        parameters = [
            Parameter(name = "transactionHash", description = "transaction hash", `in` = ParameterIn.PATH),
            Parameter(name = "signature", description = "signature hash", `in` = ParameterIn.QUERY),
        ]
    )
    @GetMapping("/api/v1/transactions/{transactionHash}/event-logs")
    fun getTransactionEventLogs(
        @PathVariable transactionHash: String,
        @RequestParam(required = false) signature: String?,
        @Valid simplePageRequest: SimplePageRequest
    ) =
        ScopePage.of(
            eventLogService.getEventLogsByTransactionHash(transactionHash, signature, simplePageRequest),
            eventLogToListViewMapper
        )

    @Operation(
        description = "Retrieve the list of fungible token transfers for a specific transaction.",
        parameters = [
            Parameter(name = "transactionHash", description = "transaction hash", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/transactions/{transactionHash}/token-transfers")
    fun getFungibleTokenTransfersOfTransaction(
        @PathVariable transactionHash: String,
        @Valid simplePageRequest: SimplePageRequest,
    ) = ScopePage.of(
        tokenService.getTokenTransfersByTransactionHash(transactionHash, simplePageRequest),
        tokenTransferToListViewMapper)

    @Operation(
        description = "Retrieve the list of non-fungible token transfers for a specific transaction.",
        parameters = [
            Parameter(name = "transactionHash", description = "transaction hash", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/transactions/{transactionHash}/nft-transfers")
    fun getNonFungibleTokenTransfersOfTransaction(
        @PathVariable transactionHash: String,
        @Valid simplePageRequest: SimplePageRequest,
    ) = ScopePage.of(
        nftService.getNftTransfersByTransactionHash(transactionHash, simplePageRequest),
        nftTransferToListViewMapper)
}
