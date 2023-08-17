package io.klaytn.finder.service.caver

import com.klaytn.caver.Caver
import com.klaytn.caver.utils.wrapper.UtilsWrapper
import java.math.BigInteger
import java.time.Duration
import org.junit.jupiter.api.Test
import org.web3j.protocol.http.HttpService

class TestCaverGovernance {
    private val okHttpClient =
            HttpService.getOkHttpClientBuilder()
                    .connectTimeout(Duration.ofSeconds(2))
                    .readTimeout(Duration.ofSeconds(1))
                    .writeTimeout(Duration.ofSeconds(3))
                    .callTimeout(Duration.ofSeconds(3))
                    .build()
    private val httpService = HttpService("http://cypress.en.klaytn.work:8551", okHttpClient)
    private val caver: Caver = Caver(httpService)
    private val utils = UtilsWrapper()

    @Test
    fun test01() {
        // ["0","86486400","90720000","95558400"]
        val idxCache = caver.rpc.governance.idxCache.send()
        println(idxCache)
    }

    @Test
    fun test02() {
        // ["0","86486400","90720000","95558400"]
        val idxCacheFromDb = caver.rpc.governance.idxCacheFromDb.send()
        println(idxCacheFromDb)
    }

    @Test
    fun test03() {
        // ["0","86486400","90720000","95558400"]
        val getItemCacheFromDb =
                caver.rpc.governance.getItemCacheFromDb(BigInteger.valueOf(95558400)).send()
        println(getItemCacheFromDb)
    }

    @Test
    fun test99() {
        val stakingInfo = caver.rpc.governance.stakingInfo.send()
        println(stakingInfo)
    }
}
