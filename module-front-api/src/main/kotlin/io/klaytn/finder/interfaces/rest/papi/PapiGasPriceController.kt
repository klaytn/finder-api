package io.klaytn.finder.interfaces.rest.papi

import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.service.GasPriceService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*

@Profile(ServerMode.PRIVATE_API_MODE)
@RestController
@Tag(name= SwaggerConstant.TAG_PRIVATE)
class PapiGasPriceController(
    private val gasPriceService: GasPriceService
) {
    @GetMapping("/papi/v1/gas-prices")
    fun getAll() = gasPriceService.getAll()

    @PostMapping("/papi/v1/account-tags/reload")
    fun reload() = gasPriceService.reload()
}