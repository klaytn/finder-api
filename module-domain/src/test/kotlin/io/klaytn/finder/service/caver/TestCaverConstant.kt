package io.klaytn.finder.service.caver

import com.klaytn.caver.Caver
import io.klaytn.commons.model.env.Phase
import org.web3j.protocol.http.HttpService
import java.time.Duration

class TestCaverAddressProxyProperty(
    val caverAddressMap: Map<TestCaverChainType, String>
)

enum class TestCaverChainType  {
    CYPRESS, BAOBAB
}

class TestCaverConstant {
    companion object {
        private val caverEnvMap = mapOf<Phase, TestCaverAddressProxyProperty>(
            Phase.prod to TestCaverAddressProxyProperty(
                caverAddressMap = mapOf(
                    TestCaverChainType.BAOBAB to "CAVER_BAOBAB_RPC_ENDPOINT",
                    TestCaverChainType.CYPRESS to "CAVER_CYPRESS_RPC_ENDPOINT")
            )
        )

        fun getCaver(phase: Phase, chain: TestCaverChainType): Caver {
            val caverEnv = caverEnvMap[phase]!!
            val okHttpClient = HttpService
                .getOkHttpClientBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .readTimeout(Duration.ofSeconds(1))
                .writeTimeout(Duration.ofSeconds(3))
                .callTimeout(Duration.ofSeconds(3))
                .build()
            val httpService = HttpService(caverEnv.caverAddressMap[chain], okHttpClient)
            return Caver(httpService)
        }
    }
}

