package io.klaytn.finder.worker.config

import io.klaytn.commons.utils.Jackson
import io.klaytn.commons.utils.okhttp.OkHttpClientBuilder
import io.klaytn.commons.utils.retrofit2.Retrofit2Creator
import io.klaytn.finder.worker.infra.client.CoinMarketCapClient
import io.klaytn.finder.worker.infra.client.CoinMarketCapInterceptor
import io.klaytn.finder.worker.infra.client.FinderPrivateApiClient
import io.klaytn.finder.worker.infra.client.SignatureClient
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.reflect.KClass
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.convert.DurationUnit
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

@Configuration
class ClientConfig(
        private val clientProperties: ClientProperties,
) {
    @Bean
    fun coinMarketCapClient() =
            createClient(
                    CoinMarketCapClient::class,
                    clientProperties.urls["coinmarketcap"]!!,
                    okHttpClient(
                            interceptor = CoinMarketCapInterceptor(),
                            requestHeaders =
                                    mapOf(
                                            "X-CMC_PRO_API_KEY" to
                                            clientProperties.keys["coinmarketcap"]!!
                                    )
                    )
            )

    @Bean
    fun signatureClient() =
            createClient(SignatureClient::class, clientProperties.urls["signature"]!!)

    @Bean
    fun finderCypressPrivateApiClient() =
            createClient(
                    FinderPrivateApiClient::class,
                    clientProperties.urls["finder-cypress-papi"]!!
            )

    @Bean
    fun finderBaobabPrivateApiClient() =
            createClient(
                    FinderPrivateApiClient::class,
                    clientProperties.urls["finder-cypress-papi"]!!
            )

    @Bean
    fun restTemplate(): RestTemplate {
        val clientHttpProperty = clientProperties.http
        val okHttpClient =
                OkHttpClientBuilder()
                        .maxRequests(clientHttpProperty.maxConnections)
                        .maxRequestsPerHost(clientHttpProperty.maxConnections)
                        .connectionTimeout(clientHttpProperty.connectionTimeoutSeconds.toMillis())
                        .readTimeout(clientHttpProperty.socketTimeoutSeconds.toMillis())
                        .build()
        return RestTemplate(OkHttp3ClientHttpRequestFactory(okHttpClient))
    }

    private fun <T : Any> createClient(
            clazz: KClass<T>,
            url: String,
            okHttpClient: OkHttpClient = okHttpClient()
    ) = Retrofit2Creator(okHttpClient, url, Jackson.mapper(), clazz).create()

    private fun okHttpClient(
            interceptor: Interceptor? = null,
            requestHeaders: Map<String, String>? = null
    ) =
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
                    }
                    .build()
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
            @DurationUnit(ChronoUnit.SECONDS) val connectionTimeoutSeconds: Duration,
            @DurationUnit(ChronoUnit.SECONDS) val socketTimeoutSeconds: Duration,
    )
}
