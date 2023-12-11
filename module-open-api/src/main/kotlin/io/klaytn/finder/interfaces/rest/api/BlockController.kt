package io.klaytn.finder.interfaces.rest.api

import com.klaytn.caver.transaction.type.TransactionType
import io.klaytn.finder.infra.exception.NotFoundBlockBurntException
import io.klaytn.finder.infra.exception.NotFoundBlockException
import io.klaytn.finder.infra.exception.NotFoundBlockRewardException
import io.klaytn.finder.infra.web.model.BlockRangeRequest
import io.klaytn.finder.infra.web.model.ScopePage
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.api.view.mapper.BlockToListViewMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.BlockToViewMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.transaction.TransactionToListViewMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.transaction.internal.InternalTransactionToListViewMapper
import io.klaytn.finder.interfaces.rest.api.view.model.block.BlockListView
import io.klaytn.finder.service.*
import io.klaytn.finder.view.mapper.BlockBurntToViewMapper
import io.klaytn.finder.view.mapper.BlockRewardToViewMapper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid
import io.klaytn.finder.config.ChainProperties

@RestController
@Tag(name = SwaggerConstant.TAG_PUBLIC)
class BlockController(
    private val blockService: BlockService,
    private val transactionService: TransactionService,
    private val internalTransactionService: InternalTransactionService,
    private val blockRangeService: BlockRangeService,
    private val blockRewardService: BlockRewardService,
    private val blockToViewMapper: BlockToViewMapper,
    private val blockToListViewMapper: BlockToListViewMapper,
    private val transactionToListViewMapper: TransactionToListViewMapper,
    private val internalTransactionToListViewMapper: InternalTransactionToListViewMapper,
    private val blockBurntToViewMapper: BlockBurntToViewMapper,
    private val blockRewardToViewMapper: BlockRewardToViewMapper,
    private val chainProperties: ChainProperties
) {
    @GetMapping("/api/v1/blocks")
    fun getBlocks(
        @Valid blockRangeRequest: BlockRangeRequest?,
        @Valid simplePageRequest: SimplePageRequest
    ): ScopePage<BlockListView> {
        val blockRangeIntervalType = BlockRangeIntervalType.BLOCK
        val blockRange = blockRangeService.getBlockRange(blockRangeRequest, blockRangeIntervalType)

        return ScopePage.of(
            blockService.getBlocks(blockRange, simplePageRequest),
            blockToListViewMapper,
            blockRangeService.getBlockRangeCondition(blockRange, blockRangeIntervalType)
        )
    }

    @Operation(
        description = "Retrieve a specific block.",
        parameters = [
            Parameter(name = "blockNumber", description = "block number", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/blocks/{blockNumber}")
    fun getBlock(
        @PathVariable blockNumber: Long
    ) =
        blockService.getBlock(blockNumber)?.let { blockToViewMapper.transform(it) }
            ?: throw NotFoundBlockException()

    @Operation(
        description = "Retrieve the latest block.",
    )
    @GetMapping("/api/v1/blocks/latest")
    fun getLatestBlock() = getBlock(blockService.getLatestNumber())

    @Operation(
        description = "Retrieve blocks generated at a specific timestamp.",
        parameters = [
            Parameter(name = "timestamp", description = "block timestamp", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/blocks/timestamps/{timestamp}")
    fun getBlockNumberByTimestamp(
        @PathVariable timestamp: Int
    ) =
        blockService.getNumberByTimestamp(timestamp)?.let { getBlock(it) } ?: throw NotFoundBlockException()

    @Operation(
        description = "Retrieve burn information for a specific block.",
    )
    @GetMapping("/api/v1/blocks/{blockNumber}/burns")
    fun getBlockBurns(
        @PathVariable blockNumber: Long,
    ) =
        blockService.getBlockBurn(blockNumber)?.let { blockBurntToViewMapper.transform(it, chainProperties.getKIP103BurntAmount()) }
            ?: throw NotFoundBlockBurntException()

    @Operation(
        description = "Retrieve burn information for the latest block.",
    )
    @GetMapping("/api/v1/blocks/latest/burns")
    fun getLatestBlockBurns() = getBlockBurns(blockService.getLatestNumber())

    @Operation(
        description = "Retrieve rewards for a specific block.",
        parameters = [
            Parameter(name = "blockNumber", description = "block number", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/blocks/{blockNumber}/rewards")
    fun getBlockRewards(
        @PathVariable blockNumber: Long
    ) =
        blockRewardService.getBlockReward(blockNumber)?.let { blockRewardToViewMapper.transform(it) }
            ?: throw NotFoundBlockRewardException()

    @Operation(
        description = "Retrieve rewards for the latest block.",
        parameters = [
            Parameter(name = "blockNumber", description = "block number", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/blocks/latest/rewards")
    fun getLatestBlockRewards() = getBlockRewards(blockService.getLatestNumber())

    @Operation(
        description = "Retrieve the list of transactions for a specific block.",
        parameters = [
            Parameter(name = "blockNumber", description = "block number", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/blocks/{blockNumber}/transactions")
    fun getTransactionsOfBlock(
        @PathVariable blockNumber: Long,
        @RequestParam(name = "type", required = false) transactionType: TransactionType?,
        @Valid simplePageRequest: SimplePageRequest,
    ) =
        ScopePage.of(
            transactionService.getTransactionsByBlockNumberAndType(blockNumber, transactionType, simplePageRequest),
            transactionToListViewMapper
        )

    @Operation(
        description = "Retrieve the list of internal transactions for a specific block.",
        parameters = [
            Parameter(name = "blockNumber", description = "block number", `in` = ParameterIn.PATH)
        ]
    )
    @GetMapping("/api/v1/blocks/{blockNumber}/internal-transactions")
    fun getInternalTransactionsOfBlock(
        @PathVariable blockNumber: Long,
        @Valid simplePageRequest: SimplePageRequest
    ) =
        ScopePage.of(
            internalTransactionService.getInternalTransactionsByBlockNumber(blockNumber, simplePageRequest),
            internalTransactionToListViewMapper
        )
}
