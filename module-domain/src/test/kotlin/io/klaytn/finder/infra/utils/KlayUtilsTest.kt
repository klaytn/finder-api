package io.klaytn.finder.infra.utils

import java.math.BigDecimal
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class KlayUtilsTest {
    @Test
    fun pebToKlay() {
        Assertions.assertEquals(BigDecimal(495), KlayUtils.pebToKlay("0x1ad5814560aa5c0000"))
        Assertions.assertEquals(BigDecimal("0.000000025"), KlayUtils.pebToKlay("25000000000"))
    }

    @Test
    fun klayToPub() {
        Assertions.assertEquals("0x1ad5814560aa5c0000", KlayUtils.klayToPeb(BigDecimal(495)))
    }

    @Test
    fun stripHexPrefix() {
        Assertions.assertEquals(
                "1ad5814560aa5c0000",
                KlayUtils.stripHexPrefix("0x1ad5814560aa5c0000")
        )
        Assertions.assertEquals(
                "1ad5814560aa5c0000",
                KlayUtils.stripHexPrefix("0X1ad5814560aa5c0000")
        )
        Assertions.assertEquals("aabbcc", KlayUtils.stripHexPrefix("aabbcc"))
    }

    @Test
    fun isHexPrefixed() {
        Assertions.assertEquals(true, KlayUtils.isHexPrefixed("0x1ad5814560aa5c0000"))
        Assertions.assertEquals(true, KlayUtils.isHexPrefixed("0X1ad5814560aa5c0000"))
        Assertions.assertEquals(false, KlayUtils.isHexPrefixed("aabbcc"))
    }
}
