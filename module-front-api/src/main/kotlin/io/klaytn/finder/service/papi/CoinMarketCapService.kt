package io.klaytn.finder.service.papi

import io.klaytn.commons.utils.retrofit2.orElseThrow
import io.klaytn.finder.domain.mysql.set4.TokenInfoRepository
import io.klaytn.finder.infra.client.CoinMarketCapClient
import io.klaytn.finder.infra.client.CoinPriceInfo
import org.springframework.stereotype.Service


@Service
class CoinMarketCapService(
    private val coinMarketCapClient: CoinMarketCapClient,
    private val tokenInfoRepository: TokenInfoRepository,
    //private val tokenTimeSeriesRepository: TokenTimeSeriesRepository
) {
    fun getTokenPriceInfo(): List<CoinPriceInfo> {
        val tokenInfoWithCmcId = tokenInfoRepository.findByCmcIdIsNotNull()
        val cmcIdsString = tokenInfoWithCmcId.map { it.cmcId }.joinToString(separator = ",")
        val tokenPriceInfo = coinMarketCapClient.getTokenPriceInfo(cmcIdsString).orElseThrow { IllegalStateException() }

        return tokenPriceInfo
    }
}