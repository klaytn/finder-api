package io.klaytn.finder.interfaces.rest.api

import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.exception.InvalidRequestException
import io.klaytn.finder.infra.exception.NotFoundContractCodeException
import io.klaytn.finder.infra.web.model.BlockRangeRequest
import io.klaytn.finder.infra.web.model.ScopePage
import io.klaytn.finder.infra.web.model.SimplePageRequest
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.api.view.mapper.BlockToBlockListViewMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.ContractCodeToItemViewMapper
import io.klaytn.finder.interfaces.rest.api.view.mapper.EventLogToListViewMapper
import io.klaytn.finder.interfaces.rest.api.view.model.EventLogListView
import io.klaytn.finder.interfaces.rest.api.view.model.block.BlockListView
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

@Profile(ServerMode.API_MODE)
@RestController
@Tag(name= SwaggerConstant.TAG_PUBLIC)
class AccountScaController(
    val accountService: AccountService,
    val contractCodeService: ContractCodeService,
    val blockService: BlockService,
    val eventLogService: EventLogService,
    val blockRangeService: BlockRangeService,
    val proposedBlockDownloader: ProposedBlockDownloader,
    val contractCodeToItemViewMapper: ContractCodeToItemViewMapper,
    val blockToBlockListViewMapper: BlockToBlockListViewMapper,
    val eventLogToListViewMapper: EventLogToListViewMapper,
) {
    @Operation(
        description = "Retrieve the code of the account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}/contract-codes")
    fun getContractContractCode(@PathVariable accountAddress: String) =
        contractCodeService.getContractCode(accountService.checkAndGetAddress(accountAddress))
            ?.let { contractCodeToItemViewMapper.transform(it) }
            ?: throw NotFoundContractCodeException()

    @Operation(
        description = "Retrieve the list of event logs for the account.",
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
        description = "Retrieve the list of proposed blocks for the account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}/proposed-blocks")
    fun getProposedBlocks(
        @PathVariable accountAddress: String,
        @Valid blockRangeRequest: BlockRangeRequest? = null,
        @Valid simplePageRequest: SimplePageRequest,
    ): ScopePage<BlockListView> {
        val blockRangeIntervalType = BlockRangeIntervalType.BLOCK
        val blockRange = blockRangeService.getBlockRange(blockRangeRequest, blockRangeIntervalType)

        return ScopePage.of(
            blockService.getBlocksByProposer(
                accountService.checkAndGetAddress(accountAddress), blockRange, simplePageRequest),
            blockToBlockListViewMapper,
            blockRangeService.getBlockRangeCondition(blockRange, blockRangeIntervalType)
        )
    }

    @Operation(
        description = "Retrieve and download the list of proposed blocks for the account.",
        parameters = [
            Parameter(name = "accountAddress", description = "account address", `in` = ParameterIn.PATH),
            Parameter(name = "date", description = "Search period ( ex. 202203 )", `in` = ParameterIn.QUERY),
        ]
    )
    @GetMapping("/api/v1/accounts/{accountAddress}/proposed-blocks/download")
    fun downloadProposedBlocks(
        @PathVariable accountAddress: String,
        @RequestParam(name = "date", required = true) date: String,
        response: HttpServletResponse,
    ) {
        val proposer = accountService.checkAndGetAddress(accountAddress)
        try {
            LocalDate.parse("${date}01", DateTimeFormatter.ofPattern("yyyyMMdd"))
        } catch (exception: Exception) {
            throw InvalidRequestException("parameter 'date' is invalid.", exception)
        }

        try {
            val filename = "proposed_blocks_${date}_$proposer.csv"
            response.contentType = "application/csv"
            response.setHeader("Content-Transfer-Encoding", "binary")
            response.setHeader("Content-Disposition", """attachment; fileName="$filename";""")

            proposedBlockDownloader.downloadFrom(proposer, date, response.outputStream)
        } finally {
            response.outputStream.flush()
            response.outputStream.close()
        }
    }
}