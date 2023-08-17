package io.klaytn.finder.interfaces.rest.papi

import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.utils.CSVUtils
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.service.papi.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

@Profile(ServerMode.PRIVATE_API_MODE)
@RestController
@Tag(name= SwaggerConstant.TAG_PRIVATE)
class PapiKrosslabAccountBookController(
    private val krosslabAccountBookService: KrosslabAccountBookService
) {
    @Operation(
        parameters = [
            Parameter(name = "accountAddressType", description = "accountAddressType", `in` = ParameterIn.PATH),
            Parameter(name = "tokenAddressType", description = "tokenAddressType", `in` = ParameterIn.PATH),
            Parameter(name = "date", description = "Search period ( ex. 202207 )", `in` = ParameterIn.QUERY),
        ]
    )
    @GetMapping("/papi/v1/krosslab/account_books/{accountAddressType}/tokens/{tokenAddressType}/transfers")
    fun getTokenTransfers(
        @PathVariable accountAddressType: KrosslabAccountAddressType,
        @PathVariable tokenAddressType: KrosslabTokenAddressType,
        @RequestParam(name = "date", required = true) date: String,
        response: HttpServletResponse
    ) {
        val tokenTransfers = krosslabAccountBookService.getTokenTransfers(accountAddressType.tokenAddress, tokenAddressType.tokenAddress, date)
        tokenTransfers.sortedBy { it.time }

        val filename = "[$date] token-transfers-${accountAddressType}-${tokenAddressType}.csv"
        response.contentType = "application/csv"
        response.setHeader("Content-Transfer-Encoding", "binary")
        response.setHeader("Content-Disposition", """attachment; fileName="$filename";""")

        val streamWriter = CSVUtils.streamWriter(FantoTokenTransfer::class.java, response.outputStream)
        try {
            streamWriter.writeAll(tokenTransfers)
        } finally {
            streamWriter.close()
            response.outputStream.flush()
            response.outputStream.close()
        }
    }

    @Operation(
        parameters = [
            Parameter(name = "accountAddressType", description = "accountAddressType", `in` = ParameterIn.PATH),
            Parameter(name = "date", description = "Search period ( ex. 202207 )", `in` = ParameterIn.QUERY),
        ]
    )
    @GetMapping("/papi/v1/krosslab/account_books/{accountAddressType}/transactions")
    fun getTokenTransactions(
        @PathVariable accountAddressType: KrosslabAccountAddressType,
        @RequestParam(name = "date", required = true) date: String,
        response: HttpServletResponse
    ) {
        val transactions = krosslabAccountBookService.getTransactions(accountAddressType.tokenAddress, date)
        val internalTransactions = krosslabAccountBookService.getInternalTransactions(accountAddressType.tokenAddress, date)

        val accountBooks = mutableListOf<FantoKlayTransaction>()
        accountBooks.addAll(transactions)
        accountBooks.addAll(internalTransactions)
        accountBooks.sortBy { it.time }

        val filename = "[$date] transactions-${accountAddressType}.csv"
        response.contentType = "application/csv"
        response.setHeader("Content-Transfer-Encoding", "binary")
        response.setHeader("Content-Disposition", """attachment; fileName="$filename";""")

        val streamWriter = CSVUtils.streamWriter(FantoKlayTransaction::class.java, response.outputStream)
        try {
            streamWriter.writeAll(accountBooks)
        } finally {
            streamWriter.close()
            response.outputStream.flush()
            response.outputStream.close()
        }
    }
}