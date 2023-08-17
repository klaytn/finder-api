package io.klaytn.finder.config

import io.klaytn.finder.infra.client.opensearch.AccountSearchClient
import io.klaytn.finder.infra.client.opensearch.ContractSearchClient
import io.klaytn.finder.infra.client.opensearch.TransactionSearchClient
import org.apache.http.HttpHost
import org.apache.http.client.config.RequestConfig
import org.opensearch.client.RestClient
import org.opensearch.client.RestHighLevelClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
@EnableConfigurationProperties(OpenSearchConfigProperties::class)
class OpenSearchConfig(
    private val openSearchConfigProperties: OpenSearchConfigProperties,
) {
    private val defaultMaxGram = 10

    @Bean
    fun restHighLevelClient() =
        with(openSearchConfigProperties) {
            RestHighLevelClient(RestClient
                .builder(HttpHost.create(hosts))
                .setHttpClientConfigCallback { httpAsyncClientBuilder ->
                    httpAsyncClientBuilder
                        .setMaxConnTotal(http.maxTotalConnections)
                        .setMaxConnPerRoute(http.maxConnectionPerRoute)
                        .setConnectionTimeToLive(
                            http.connectionTimeToLive.toMillis(),
                            TimeUnit.MILLISECONDS
                        )
                        .setDefaultRequestConfig(
                            RequestConfig
                                .custom()
                                .setConnectionRequestTimeout(http.connectionRequestTimeout.toMillis().toInt())
                                .setConnectTimeout(http.connectionTimeout.toMillis().toInt())
                                .setSocketTimeout(http.socketTimeout.toMillis().toInt())
                                .build()
                        )
                })
        }

    @Bean
    fun accountSearchClient() =
        AccountSearchClient(restHighLevelClient(), openSearchConfigProperties.index.account, defaultMaxGram)

    @Bean
    fun contractSearchClient() =
        ContractSearchClient(restHighLevelClient(), openSearchConfigProperties.index.contract, defaultMaxGram)

    @Bean
    fun transactionSearchClient() =
        TransactionSearchClient(restHighLevelClient(), openSearchConfigProperties.index.transaction)
}

@ConstructorBinding
@ConfigurationProperties(prefix = "opensearch.finder")
data class OpenSearchConfigProperties(
    val hosts: String,
    val http: HttpProperties,
    val index: Index
){
    data class HttpProperties (
        val connectionRequestTimeout: Duration,
        val connectionTimeout: Duration,
        val connectionTimeToLive: Duration,
        val socketTimeout: Duration,
        val maxTotalConnections: Int,
        val maxConnectionPerRoute: Int,
    )

    data class Index (
        val account: String,
        val contract: String,
        val transaction: String,
    )
}