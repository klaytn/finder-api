package io.klaytn.finder.interfaces.rest.api

import com.klaytn.caver.transaction.type.TransactionType
import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.exception.NotFoundBlockBurntException
import io.klaytn.finder.infra.exception.NotFoundBlockException
import io.klaytn.finder.infra.exception.NotFoundBlockRewardException
import io.klaytn.finder.infra.web.model.BlockRangeRequest
import io.klaytn.finder.infra.web.model.ScopePage
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.api.view.mapper.*
import io.klaytn.finder.interfaces.rest.api.view.model.block.BlockListView
import io.klaytn.finder.service.*
import io.klaytn.finder.view.mapper.BlockBurntToViewMapper
import io.klaytn.finder.view.mapper.BlockRewardToViewMapper
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
class BlockController(
    val blockService: BlockService,
    val transactionService: TransactionService,
    val internalTransactionService: InternalTransactionService,
    val blockRangeService: BlockRangeService,
    val blockRewardService: BlockRewardService,
    val blockToBlockListViewMapper: BlockToBlockListViewMapper,
    val blockToBlockItemViewMapper: BlockToBlockItemViewMapper,
    val transactionToListViewMapper: TransactionToListViewMapper,
    val internalTransactionToListViewMapper: InternalTransactionToListViewMapper,
    val blockBurntToViewMapper: BlockBurntToViewMapper,
    val blockRewardToViewMapper: BlockRewardToViewMapper
) {
    @Operation(
        description = "Retrieve a list of blocks.",
    )
    @GetMapping("/api/v1/blocks")
    fun getBlocks(
        @Valid blockRangeRequest: BlockRangeRequest?,
        @Valid simplePageRequest: SimplePageRequest
    ): ScopePage<BlockListView> {
        val blockRangeIntervalType = BlockRangeIntervalType.BLOCK
        val blockRange = blockRangeService.getBlockRange(blockRangeRequest, blockRangeIntervalType)

        return ScopePage.of(
            blockService.getBlocks(blockRange, simplePageRequest),
            blockToBlockListViewMapper,
            blockRangeService.getBlockRangeCondition(blockRange, blockRangeIntervalType)
        )
    }

    @Operation(
        description = "Retrieve a specific block.",
        parameters = [
            Parameter(name = "blockNumber", description = "블럭 번호", `in` = ParameterIn.PATH)
        ]
    )
    @GetMapping("/api/v1/blocks/{blockNumber}")
    fun getBlock(@PathVariable blockNumber: Long) =
        blockService.getBlock(blockNumber)?.let { blockToBlockItemViewMapper.transform(it) }
            ?: throw NotFoundBlockException()

    @Operation(
        description = "Retrieve burn information for a specific block.",
    )
    @GetMapping("/api/v1/blocks/{blockNumber}/burns")
    fun getBlockBurns(
        @PathVariable blockNumber: Long,
    ) = blockService.getBlockBurn(blockNumber)?.let { blockBurntToViewMapper.transform(it) }
        ?: throw NotFoundBlockBurntException()

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
        description = "Retrieve transactions for a specific block.",
        parameters = [
            Parameter(name = "blockNumber", description = "block number", `in` = ParameterIn.PATH),
            Parameter(name = "type", description = "transaction type", `in` = ParameterIn.QUERY)
        ]
    )
    @GetMapping("/api/v1/blocks/{blockNumber}/transactions")
    fun getBlockTransactions(
        @PathVariable blockNumber: Long,
        @RequestParam(name = "type", required = false) type: TransactionType?,
        @Valid simplePageRequest: SimplePageRequest,
    ) =
        ScopePage.of(
            transactionService.getTransactionsByBlockNumberAndType(blockNumber, type, simplePageRequest),
            transactionToListViewMapper
        )

    @Operation(
        description = "Retrieve internal transactions for a specific block number.",
        parameters = [
            Parameter(name = "blockNumber", description = "block number", `in` = ParameterIn.PATH)
        ]
    )
    @GetMapping("/api/v1/blocks/{blockNumber}/internal-transactions")
    fun getBlockInternalTransactions(
        @PathVariable blockNumber: Long,
        @Valid simplePageRequest: SimplePageRequest) =
        ScopePage.of(
            internalTransactionService.getInternalTransactionsByBlockNumber(blockNumber, simplePageRequest),
            internalTransactionToListViewMapper
        )
}
