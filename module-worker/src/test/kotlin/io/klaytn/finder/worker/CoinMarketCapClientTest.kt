package io.klaytn.finder.worker

import io.klaytn.commons.utils.Jackson
import io.klaytn.commons.utils.okhttp.OkHttpClientBuilder
import io.klaytn.commons.utils.retrofit2.Retrofit2Creator
import io.klaytn.commons.utils.retrofit2.orElseThrow
import io.klaytn.finder.worker.infra.client.CoinMarketCapClient
import io.klaytn.finder.worker.infra.client.CoinMarketCapInterceptor
import org.junit.jupiter.api.Test

class CoinMarketCapClientTest {
    @Test
    fun test() {
        val okHttpClient = OkHttpClientBuilder()
            .addInterceptor(CoinMarketCapInterceptor())
            .addHeader("X-CMC_PRO_API_KEY" to "CMC_PRO_API_KEY")
            .build()
        val coinMarketCapClient =
            Retrofit2Creator(okHttpClient,
                "https://pro-api.coinmarketcap.com/",
                Jackson.mapper(),
                CoinMarketCapClient::class).create()
        val aa = coinMarketCapClient.getPrice("4256", "USD").orElseThrow { IllegalStateException() }
        println(aa)
    }
}