package io.klaytn.finder.interfaces.rest.api

import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.service.FinderHomeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Profile(ServerMode.API_MODE)
@RestController
@Tag(name= SwaggerConstant.TAG_PUBLIC)
class FinderHomeController(
    private val finderHomeService: FinderHomeService,
) {
    @Operation(description = "Transaction History")
    @GetMapping("/api/v1/home/txhistory")
    fun getTransactionHistories() = finderHomeService.getTransactionHistory()

    @Operation(description = "Gas Fee Burn History")
    @GetMapping("/api/v1/home/burnt-by-gas-fee-history")
    fun getBurntByGasFeeHistory() = finderHomeService.getBurntByGasFeeHistory()
}
