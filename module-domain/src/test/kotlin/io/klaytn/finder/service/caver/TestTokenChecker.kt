package io.klaytn.finder.service.caver

import com.klaytn.caver.Caver
import com.klaytn.caver.kct.kip7.KIP7
import com.zaxxer.hikari.HikariDataSource
import io.klaytn.commons.model.env.Phase
import io.klaytn.finder.infra.utils.applyDecimal
import io.klaytn.finder.service.db.TestDbConstant
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class TestTokenChecker {
    @Test
    fun getCypressMBXBalanceOf() {
        val caver = TestCaverConstant.getCaver(Phase.prod, TestCaverChainType.CYPRESS)
        val dataSource = TestDbConstant.getDatasource(
            Phase.prod, TestCaverChainType.CYPRESS, TestDbConstant.TestDbType.SET3, true)
        val contractAddress = "0xd068c52d81f4409b9502da926ace3301cc41f623"

        val holders = getHolders(dataSource, contractAddress)
        val balanceMap = getBalanceOf(caver, contractAddress, holders)

        balanceMap.forEach {
            println("${it.key} -> ${it.value}")
        }
    }

    private fun getHolders(dataSource: HikariDataSource, contractAddress: String): List<String> {
        val holders = mutableListOf<String>()
        dataSource.use {
            it.connection.use { conn ->
                conn.prepareStatement(
                    """
                    select 
                        holder_address 
                    from 
                        token_holders 
                    where 
                        contract_address='$contractAddress' 
                    order by 
                        amount desc 
                    limit 20
                    """.trimIndent()).use { psmt ->
                    psmt.executeQuery().use { resultSet ->
                        while(resultSet.next()) {
                            holders.add(resultSet.getString(1))
                        }
                    }
                }
            }
        }
        return holders
    }

    private fun getBalanceOf(caver: Caver, contractAddress: String, holders: List<String>): Map<String, BigDecimal> {
        val kip7 = KIP7.create(caver, contractAddress)
        val decimal = kip7.decimals()

        return holders.associateWith { holder ->
            kip7.balanceOf(holder).toBigDecimal().applyDecimal(decimal)
        }
    }
}
