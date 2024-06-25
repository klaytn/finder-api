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
    fun getTokenPriceInfo(): Boolean {
        val tokenInfoWithCmcId = tokenInfoRepository.findByCmcIdIsNotNull()
        val cmcIdsString = tokenInfoWithCmcId.map { it.cmcId }.joinToString(separator = ",")
        val tokenPriceInfoMap =
            coinMarketCapClient.getTokenPriceInfo(cmcIdsString).orElseThrow { IllegalStateException() }
                .associateBy { it.id }

        val klayPrice = tokenPriceInfoMap[4256]?.price?.toBigDecimal() ?: BigDecimal.ZERO

        val contractList = tokenInfoWithCmcId.map { it.contractAddress }
        val contracts = contractService.getContracts(contractList)

        val combinedList = tokenInfoWithCmcId.mapNotNull { tokenInfo ->
            val contract = contracts.find { it.contractAddress == tokenInfo.contractAddress }
            val tokenPriceInfo = tokenPriceInfoMap[tokenInfo.cmcId]

            val onChainMarketCap =
                (contract?.totalSupply?.multiply(tokenPriceInfo?.price?.toBigDecimalOrNull() ?: BigDecimal.ZERO)).toString()
            val circulatingMarketCap =
                ((tokenPriceInfo?.circulatingsupply?.toBigDecimalOrNull() ?: BigDecimal.ZERO).multiply(tokenPriceInfo?.price?.toBigDecimalOrNull() ?: BigDecimal.ZERO)).toPlainString()

            if (contract != null && tokenPriceInfo != null) {
                val tokenPrice = tokenPriceInfo.price.toBigDecimal() ?: BigDecimal.ZERO

                val kaiaPrice = if (tokenPrice > BigDecimal.ZERO) {
                    tokenPrice.divide(klayPrice, 18, BigDecimal.ROUND_HALF_UP).toString()
                } else {
                    BigDecimal.ZERO.toString()
                }

                TokenTimeSeries(
                    tokenInfoId = tokenInfo.id,
                    symbol = tokenInfo.symbol,
                    price = tokenPriceInfo.price,
                    kaiaPrice,
                    changeRate = tokenPriceInfo.percentchange24h,
                    volume = tokenPriceInfo.volume24h,
                    marketCap = tokenPriceInfo.marketcap,
                    onChainMarketCap,
                    circulatingMarketCap,
                    timestamp = (System.currentTimeMillis() / 1000).toInt(),
                )
            } else {
                null
            }
        }

        tokenTimeSeriesRepository.saveAll(combinedList)

        return true
    }
}