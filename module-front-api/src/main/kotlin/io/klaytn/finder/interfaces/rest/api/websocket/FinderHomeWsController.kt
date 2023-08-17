package io.klaytn.finder.interfaces.rest.api.websocket

import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.service.FinderHomeService
import org.springframework.context.annotation.Profile
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.stereotype.Controller

@Profile(ServerMode.API_MODE)
@Controller
class FinderHomeWsController(
    private val finderHomeService: FinderHomeService,
) {
    @SubscribeMapping("/status")
    fun getStatus() = finderHomeService.getStatus()

    @SubscribeMapping("/summary")
    fun getSummary() = finderHomeService.getSummary()

    @SubscribeMapping("/klay-price")
    fun getKlayPrice() = finderHomeService.getKlayPrice()
}
