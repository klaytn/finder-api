package io.klaytn.finder.interfaces.rest.papi

import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.service.papi.CoinMarketCapService

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Profile(ServerMode.PRIVATE_API_MODE)
@RestController
@Tag(name = SwaggerConstant.TAG_PRIVATE)
class PapiCoinMarketCapController(val coinMarketCapService: CoinMarketCapService) {
    @GetMapping("/papi/v1/coin-market-cap/cryptocurrency")
    fun getTokenInfo() = coinMarketCapService.getTokenPriceInfo()
}