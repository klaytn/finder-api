package io.klaytn.finder.config

import io.klaytn.commons.utils.Jackson
import io.klaytn.commons.utils.okhttp.OkHttpClientBuilder
import io.klaytn.commons.utils.retrofit2.Retrofit2Creator
import io.klaytn.finder.infra.client.CoinMarketCapClient
import io.klaytn.finder.infra.client.CoinMarketCapInterceptor
import io.klaytn.finder.infra.client.ContractCompilerClient
import io.klaytn.finder.infra.client.KlaytnSquareClient
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.convert.DurationUnit
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.reflect.KClass

@Configuration
class ClientConfig(
    private val clientProperties: ClientProperties,
) {
    @Bean
    fun contractCompilerClient() =
        createClient(ContractCompilerClient::class, clientProperties.urls["contract-compiler"]!!)

    @Bean
    fun klaytnSquareClient() =
        createClient(KlaytnSquareClient::class, clientProperties.urls["square-api"]!!)

    @Bean
    fun coinMarketCapClient() =
        createClient(
            CoinMarketCapClient::class,
            clientProperties.urls["coin-market-cap"]!!,
            okHttpClient(
                interceptor = CoinMarketCapInterceptor(),
                requestHeaders =
                mapOf(
                    "X-CMC_PRO_API_KEY" to
                            clientProperties.keys["coin-market-cap"]!!
                )
            )
        )

    private fun <T : Any> createClient(clazz: KClass<T>, url: String, okHttpClient: OkHttpClient = okHttpClient()) =
        Retrofit2Creator(okHttpClient, url, Jackson.mapper(), clazz).create()

    private fun okHttpClient(interceptor: Interceptor? = null, requestHeaders: Map<String, String>? = null) =
        OkHttpClientBuilder()
            .maxRequests(clientProperties.http.maxConnections)
            .maxRequestsPerHost(clientProperties.http.maxConnections)
            .connectionTimeout(clientProperties.http.connectionTimeoutSeconds.toMillis())
            .readTimeout(clientProperties.http.socketTimeoutSeconds.toMillis())
            .addInterceptor(interceptor)
            .apply {
                if (!requestHeaders.isNullOrEmpty()) {
                    requestHeaders.forEach { addHeader(it.key to it.value) }
                }
            }.build()
}

@ConstructorBinding
@ConfigurationProperties(prefix = "finder.clients")
data class ClientProperties(
    val http: ClientHttpProperties,
    val urls: Map<String, String> = mapOf(),
    val keys: Map<String, String> = mapOf(),
) {
    data class ClientHttpProperties(
        val maxConnections: Int,

        @DurationUnit(ChronoUnit.SECONDS)
        val connectionTimeoutSeconds: Duration,

        @DurationUnit(ChronoUnit.SECONDS)
        val socketTimeoutSeconds: Duration,
    )
}
