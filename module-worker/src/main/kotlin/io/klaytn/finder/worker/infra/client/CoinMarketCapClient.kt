package io.klaytn.finder.worker.infra.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.math.BigDecimal
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CoinMarketCapClient {
    @GET("/v2/cryptocurrency/quotes/latest")
    fun getPrice(@Query("id") id: String, @Query("convert") unit: String): Call<CoinPrice>
}

data class CoinPrice(
    val id: Int,
    val name: String,
    val symbol: String,
    val slug: String,
    val numberOfMarketPairs: Int,
    val circulatingSupply: BigDecimal,
    val totalSupply: BigDecimal,
    //    val dateAdded: LocalDateTime,
    //    val lastUpdated: LocalDateTime,
    val price: BigDecimal,
    val marketCap: BigDecimal,
    val marketCapDominance: BigDecimal,
    val percentChange24h: BigDecimal,
    val volume24h: BigDecimal,
)

class CoinMarketCapInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val mapper = jacksonObjectMapper()

        val request = chain.request()
        val response = chain.proceed(request)
        val id = request.url.queryParameterValues("id")[0]
        val unit = request.url.queryParameterValues("convert")[0]

        val json = response.body?.string() ?: "{}"
        val tree = mapper.readTree(json).at("/data/$id")

        val coinPrice =
            CoinPrice(
                id = tree.at("/id").asInt(),
                name = tree.at("/name").asText(),
                symbol = tree.at("/symbol").asText(),
                slug = tree.at("/slug").asText(),
                numberOfMarketPairs = tree.at("/num_market_pairs").asInt(),
                circulatingSupply = BigDecimal(tree.at("/circulating_supply").asText()),
                totalSupply = BigDecimal(tree.at("/total_supply").asText()),
                price = BigDecimal(tree.at("/quote/$unit/price").asText()),
                marketCap = BigDecimal(tree.at("/quote/$unit/market_cap").asText()),
                marketCapDominance =
                BigDecimal(tree.at("/quote/$unit/market_cap_dominance").asText()),
                percentChange24h =
                BigDecimal(tree.at("/quote/$unit/percent_change_24h").asText()),
                volume24h = BigDecimal(tree.at("/quote/$unit/volume_24h").asText()),
            )

        return response.newBuilder()
            .message(response.message)
            .body(
                mapper.writeValueAsString(coinPrice)
                    .toResponseBody(response.body?.contentType())
            )
            .build()
    }
}
