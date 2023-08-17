package io.klaytn.finder.interfaces.rest.api

import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.interfaces.rest.api.view.model.KlaytnView
import io.klaytn.finder.service.FinderHomeService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Requirements : https://klayon.slack.com/archives/C033P14KD7T/p1669787982820479
 * - Transaction per second
 * - Avg Block Time(24h)
 * - Avg TX per Block(24h)
 * - Consensus Nodes
 * - KLAY Price
 * - Market cap
 */
@RestController
@Tag(name = SwaggerConstant.TAG_PUBLIC)
class KlaytnController(
    private val finderHomeService: FinderHomeService,
) {
    @GetMapping("/api/v1/klaytn")
    fun getHome() = KlaytnView(finderHomeService.getSummary(), finderHomeService.getKlayPrice())
}
