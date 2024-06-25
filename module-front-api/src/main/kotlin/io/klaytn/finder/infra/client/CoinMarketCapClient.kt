package io.klaytn.finder.infra.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import java.math.BigDecimal

interface CoinMarketCapClient {
    @GET("/v2/cryptocurrency/quotes/latest")
    fun getTokenPriceInfo(@Query("id") id: String): Call<List<CoinPriceInfo>>
}

data class CoinPriceInfo(
    val id: Int,
    val name: String,
    val symbol: String,
    val circulatingsupply: String,
    val price: String,
    val volume24h: String,
    val percentchange24h: String,
    val marketcap: String,
)

class CoinMarketCapInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val mapper = jacksonObjectMapper()
        val request = chain.request()
        val response = chain.proceed(request)
        val json = response.body?.string() ?: "{}"
        val tree = mapper.readTree(json).at("/data")

        val coinInfoList = tree.fields().asSequence().map { entry ->
            val node = entry.value
            val circulatingsupply = node.at("/circulating_supply").asText().takeIf { it.isNotEmpty() }?.let {
                try {
                    BigDecimal(it).toPlainString()
                } catch (e: NumberFormatException) {
                    "0"
                }
            } ?: "0"

            val price = node.at("/quote/USD/price").asText().takeIf { it.isNotEmpty() }?.let {
                try {
                    BigDecimal(it).toPlainString()
                } catch (e: NumberFormatException) {
                    "0"
                }
            } ?: "0"

            val volume24h = node.at("/quote/USD/volume_24h").asText().takeIf { it.isNotEmpty() }?.let {
                try {
                    BigDecimal(it).toPlainString()
                } catch (e: NumberFormatException) {
                    "0"
                }
            } ?: "0"

            val percentchange24h = node.at("/quote/USD/percent_change_24h").asText().takeIf { it.isNotEmpty() }?.let {
                try {
                    BigDecimal(it).toPlainString()
                } catch (e: NumberFormatException) {
                    "0"
                }
            } ?: "0"

            val marketcap = node.at("/quote/USD/market_cap").asText().takeIf { it.isNotEmpty() }?.let {
                try {
                    BigDecimal(it).toPlainString()
                } catch (e: NumberFormatException) {
                    "0"
                }
            } ?: "0"

            CoinPriceInfo(
                id = node.at("/id").asInt(),
                name = node.at("/name").asText(),
                symbol = node.at("/symbol").asText(),
                circulatingsupply,
                price,
                volume24h,
                percentchange24h,
                marketcap
            )
        }.toList()

        return response.newBuilder()
            .message(response.message)
            .body(mapper.writeValueAsString(coinInfoList).toResponseBody(response.body?.contentType()))
            .build()
    }
}