package io.klaytn.finder.service

import java.math.BigDecimal
import java.math.RoundingMode
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BlockRewardTest {
    private val defaultBlockMintValue = BigDecimal(9.6)

    @Test
    fun test() {
        val gasUsed = 2433106
        val gasPrice = BigDecimal("0.00000025")
        val fees = gasUsed.toBigDecimal() * gasPrice

        val reward = (defaultBlockMintValue + fees.divide(BigDecimal(2)))
        val burntFees = fees.divide(BigDecimal(2))

        Assertions.assertEquals(BigDecimal("0.6082765000"), fees.setScale(10, RoundingMode.HALF_UP))
        Assertions.assertEquals(
                BigDecimal("0.3041382500"),
                burntFees.setScale(10, RoundingMode.HALF_UP)
        )
        Assertions.assertEquals(
                BigDecimal("9.9041382500"),
                reward.setScale(10, RoundingMode.HALF_UP)
        )
    }

    @Test
    fun test2() {
        val gasUsed = 302555
        val gasPrice = BigDecimal("0.00000025")
        val fees = gasUsed.toBigDecimal() * gasPrice
        val mintedKlay = BigDecimal(9.6)

        val reward = (defaultBlockMintValue + fees.divide(BigDecimal(2)))
        val burntFees = fees.divide(BigDecimal(2))

        Assertions.assertEquals(BigDecimal("0.0756387500"), fees.setScale(10, RoundingMode.HALF_UP))
        Assertions.assertEquals(
                BigDecimal("0.0378193750"),
                burntFees.setScale(10, RoundingMode.HALF_UP)
        )
        Assertions.assertEquals(
                BigDecimal("9.6378193750"),
                reward.setScale(10, RoundingMode.HALF_UP)
        )
    }

    @Test
    fun test3() {
        val fees = BigDecimal("0.07563875")

        val reward = (defaultBlockMintValue + fees.divide(BigDecimal(2)))
        val burntFees = fees.divide(BigDecimal(2))

        Assertions.assertEquals(
                BigDecimal("0.0378193750"),
                burntFees.setScale(10, RoundingMode.HALF_UP)
        )
        Assertions.assertEquals(
                BigDecimal("9.6378193750"),
                reward.setScale(10, RoundingMode.HALF_UP)
        )
    }
}
