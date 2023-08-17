package io.klaytn.finder.worker.service

import io.klaytn.commons.utils.retrofit2.orElseThrow
import io.klaytn.finder.worker.infra.client.CoinMarketCapClient
import org.springframework.stereotype.Service

@Service
class CoinPriceService(
    private val coinMarketCapClient: CoinMarketCapClient
) {
    fun getKlayPrice(unit: String) =
        coinMarketCapClient.getPrice("4256", unit).orElseThrow { IllegalStateException() }
}
