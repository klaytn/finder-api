package io.klaytn.finder.config

import com.klaytn.caver.Caver
import java.math.BigDecimal
import java.net.InetSocketAddress
import java.net.Proxy
import java.time.Duration
import java.time.temporal.ChronoUnit
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.convert.DurationUnit
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.web3j.protocol.http.HttpService

@Configuration
@EnableConfigurationProperties(ChainProperties::class)
class ChainConfig(private val chainProperties: ChainProperties) {
    @Bean
    fun caver(
            @Value("\${caver.proxy.host:}") caverProxyHost: String,
            @Value("\${caver.proxy.port:3128}") caverProxyPort: Int,
    ): Caver {
        val caverProperties = chainProperties.caver

        val proxy: Proxy? =
                if (caverProxyHost.isNotBlank()) {
                    val proxySocketAddress = InetSocketAddress(caverProxyHost, caverProxyPort)
                    Proxy(Proxy.Type.HTTP, proxySocketAddress)
                } else {
                    null
                }

        val okHttpClient =
                HttpService.getOkHttpClientBuilder()
                        .connectTimeout(caverProperties.connectionTimeoutSeconds)
                        .readTimeout(caverProperties.readTimeoutSeconds)
                        .writeTimeout(caverProperties.writeTimeoutSeconds)
                        .callTimeout(caverProperties.callTimeoutSeconds)
                        .proxy(proxy)
                        .build()
        val httpService = HttpService(caverProperties.url, okHttpClient)
        return Caver(httpService)
    }
}

@ConstructorBinding
@ConfigurationProperties(prefix = "finder.chain")
data class ChainProperties(
        /** chain type( cypress, baobab ) */
        val type: String,
        val caver: CaverProperty,
        val blockMintProperties: List<ChainBlockMintProperty>,
        val managedAddress: Map<String, String>?,
        val hardFork: Map<ChainHardFortType, Long>,
) {
    data class CaverProperty(
            val url: String,
            @DurationUnit(ChronoUnit.SECONDS) val connectionTimeoutSeconds: Duration,
            @DurationUnit(ChronoUnit.SECONDS) val readTimeoutSeconds: Duration,
            @DurationUnit(ChronoUnit.SECONDS) val writeTimeoutSeconds: Duration,
            @DurationUnit(ChronoUnit.SECONDS) val callTimeoutSeconds: Duration,
    )

    fun getManagedAddress(type: String) = managedAddress?.get(type)

    fun isInHardForkRange(type: ChainHardFortType, blockNumber: Long): Boolean {
        val hardForkBlockNumber = hardFork[type] ?: 0L
        return hardForkBlockNumber != 0L && blockNumber >= hardForkBlockNumber
    }

    fun isDynamicFeeTarget(blockNumber: Long) =
            isInHardForkRange(ChainHardFortType.MAGMA, blockNumber)
}

data class ChainBlockMintProperty(val startBlockNumber: Long, val mintValue: BigDecimal)

enum class ChainHardFortType {
    MAGMA,
    KORE
}
