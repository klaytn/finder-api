package io.klaytn.finder.infra.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CoinMarketCapClient {
    @GET("/v2/cryptocurrency/quotes/latest")
    fun getTokenPriceInfo(@Query("id") id: String): Call<List<CoinPriceInfo>>
}

data class CoinPriceInfo(
    val id: Int,
    val name: String,
    val symbol: String,
    val price: Double,
    val volume24h: Double,
    val percentchange24h: Double,
    val marketcap: Double,
)

class CoinMarketCapInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val mapper = jacksonObjectMapper()
        val request = chain.request()
        val response = chain.proceed(request)
        val json = response.body?.string() ?: "{}"
        val tree = mapper.readTree(json).at("/data")
        println(tree)
        val coinInfoList = tree.fields().asSequence().map { entry ->
            val node = entry.value
            CoinPriceInfo(
                id = node.at("/id").asInt(),
                name = node.at("/name").asText(),
                symbol = node.at("/symbol").asText(),
                price = node.at("/quote/USD/price").asDouble(),
                volume24h = node.at("/quote/USD/volume_24h").asDouble(),
                percentchange24h = node.at("/quote/USD/percent_change_24h").asDouble(),
                marketcap = node.at("/quote/USD/market_cap").asDouble()
            )
        }.toList()
        println(coinInfoList)

        return response.newBuilder()
            .message(response.message)
            .body(mapper.writeValueAsString(coinInfoList).toResponseBody(response.body?.contentType()))
            .build()
    }
}