package io.klaytn.finder.interfaces.rest.api

import com.klaytn.caver.transaction.type.TransactionType
import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.client.opensearch.model.TransactionSearchPageRequest
import io.klaytn.finder.infra.client.opensearch.model.TransactionSearchRequest
import io.klaytn.finder.infra.exception.NotFoundTransactionException
import io.klaytn.finder.infra.web.model.BlockRangeRequest
import io.klaytn.finder.infra.web.model.ScopePage
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.api.view.mapper.*
import io.klaytn.finder.interfaces.rest.api.view.model.transaction.TransactionListView
import io.klaytn.finder.service.*
import io.klaytn.finder.service.nft.NftService
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
class TransactionController(
    val transactionService: TransactionService,
    val eventLogService: EventLogService,
    val internalTransactionService: InternalTransactionService,
    val tokenService: TokenService,
    val nftService: NftService,
    val blockRangeService: BlockRangeService,
    val transactionToListViewMapper: TransactionToListViewMapper,
    val transactionToItemViewMapper: TransactionToItemViewMapper,
    val transactionToInputDataViewMapper: TransactionToInputDataViewMapper,
    val eventLogToListViewMapper: EventLogToListViewMapper,
    val internalTransactionToLeveledListViewMapper: InternalTransactionToLeveledListViewMapper,
    val tokenTransferToListViewMapper: TokenTransferToListViewMapper,
    val nftTransferToListViewMapper: NftTransferToListViewMapper,
) {
    @Operation(
        description = "Retrieves a list of transactions.",
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
        description = "Searches for transactions.",
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
        description = "Retrieves a specific transaction.",
        parameters = [
            Parameter(name = "transactionHash", description = "transaction hash", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/transactions/{transactionHash}")
    fun getTransactions(@PathVariable transactionHash: String) =
        transactionService.getTransactionByHash(transactionHash)
            ?.let { transactionToItemViewMapper.transform(it) }
            ?: throw NotFoundTransactionException()

    @Operation(
        description = "Retrieves the internal transactions of a specific transaction.",
        parameters = [
            Parameter(name = "transactionHash", description = "transaction hash", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/transactions/{transactionHash}/internal-transactions")
    fun getInternalTransactions(@PathVariable transactionHash: String, @Valid simplePageRequest: SimplePageRequest) =
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
        description = "Retrieves the event logs of a specific transaction.",
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
        description = "Retrieves the input data of a specific transaction.",
        parameters = [
            Parameter(name = "transactionHash", description = "transaction hash", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/transactions/{transactionHash}/input-data")
    fun getTransactionInputData(@PathVariable transactionHash: String) =
        transactionService.getTransactionByHash(transactionHash)?.let { transaction ->
            transaction.input?.let {
                transactionToInputDataViewMapper.transform(transaction)
            }
        }

    @Operation(
        description = "Retrieves the token transfer list of a specific transaction.",
        parameters = [
            Parameter(name = "transactionHash", description = "transaction hash", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/transactions/{transactionHash}/token-transfers")
    fun getTransactionTokenTransfers(
        @PathVariable transactionHash: String,
        @Valid simplePageRequest: SimplePageRequest,
    ) = ScopePage.of(
        tokenService.getTokenTransfersByTransactionHash(transactionHash, simplePageRequest),
        tokenTransferToListViewMapper)

    @Operation(
        description = "Retrieves the NFT transfer list of a specific transaction.",
        parameters = [
            Parameter(name = "transactionHash", description = "transaction hash", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/transactions/{transactionHash}/nft-transfers")
    fun getTransactionNftTransfers(
        @PathVariable transactionHash: String,
        @Valid simplePageRequest: SimplePageRequest,
    ) = ScopePage.of(
        nftService.getNftTransfersByTransactionHash(transactionHash, simplePageRequest),
        nftTransferToListViewMapper)
}
