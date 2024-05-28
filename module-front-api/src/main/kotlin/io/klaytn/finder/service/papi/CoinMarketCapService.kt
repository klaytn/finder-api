package io.klaytn.finder.service.papi

import io.klaytn.commons.utils.retrofit2.orElseThrow
import io.klaytn.finder.domain.mysql.set4.TokenInfoRepository
import io.klaytn.finder.domain.mysql.set4.TokenTimeSeries
import io.klaytn.finder.domain.mysql.set4.TokenTimeSeriesRepository
import io.klaytn.finder.infra.client.CoinMarketCapClient
import io.klaytn.finder.service.ContractService
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class CoinMarketCapService(
    private val coinMarketCapClient: CoinMarketCapClient,
    private val tokenInfoRepository: TokenInfoRepository,
    private val tokenTimeSeriesRepository: TokenTimeSeriesRepository,
    private val contractService: ContractService,
) {
    fun getTokenPriceInfo(): List<TokenTimeSeries> {
        val tokenInfoWithCmcId = tokenInfoRepository.findByCmcIdIsNotNull()
        val cmcIdsString = tokenInfoWithCmcId.map { it.cmcId }.joinToString(separator = ",")
        val tokenPriceInfoMap =
            coinMarketCapClient.getTokenPriceInfo(cmcIdsString).orElseThrow { IllegalStateException() }
                .associateBy { it.id }

        val contractList = tokenInfoWithCmcId.map { it.contractAddress }
        val contracts = contractService.getContracts(contractList)

        val combinedList = tokenInfoWithCmcId.mapNotNull { tokenInfo ->
            val contract = contracts.find { it.contractAddress == tokenInfo.contractAddress }
            val tokenPriceInfo = tokenPriceInfoMap[tokenInfo.cmcId]

            val onChainMarketCap =
                (contract?.totalSupply?.multiply(tokenPriceInfo?.price?.toBigDecimal() ?: BigDecimal.ZERO)).toString()

            if (contract != null && tokenPriceInfo != null) {
                TokenTimeSeries(
                    tokenInfoId = tokenInfo.id,
                    symbol = tokenInfo.symbol,
                    price = tokenPriceInfo.price.toString(),
                    kaiaPrice = "0",
                    changeRate = tokenPriceInfo.percentchange24h.toString(),
                    volume = tokenPriceInfo.volume24h.toString(),
                    marketCap = tokenPriceInfo.marketcap.toString(),
                    onChainMarketCap,
                    timestamp = (System.currentTimeMillis() / 1000).toInt(),
                )
            } else {
                null
            }
        }

        tokenTimeSeriesRepository.saveAll(combinedList)

        return combinedList
    }
}