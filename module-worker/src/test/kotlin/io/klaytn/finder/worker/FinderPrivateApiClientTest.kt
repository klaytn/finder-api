package io.klaytn.finder.worker

import io.klaytn.commons.utils.retrofit2.orElseThrow
import io.klaytn.finder.worker.config.ClientConfig
import io.klaytn.finder.worker.config.ClientProperties
import io.klaytn.finder.worker.infra.client.FinderPrivateApiClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

class FinderPrivateApiClientTest {
    lateinit var client: FinderPrivateApiClient

    @BeforeEach
    fun setup() {
        val clientProperties = ClientProperties(
            urls = mapOf(
                "finder-cypress-papi" to "http://localhost:8080",
            ),
            http = ClientProperties.ClientHttpProperties(
                1000,
                Duration.ofSeconds(10),
                Duration.ofSeconds(10)
            )
        )

        val clientConfig = ClientConfig(clientProperties)
        client = clientConfig.finderCypressPrivateApiClient()
    }

    @Test
    fun addFunctionSignature() {
        client.addFunctionSignature(844375, "0xd30df037", "setSplits((uint256,uint32)[])")
            .orElseThrow { message -> IllegalStateException(message) }
    }
}
