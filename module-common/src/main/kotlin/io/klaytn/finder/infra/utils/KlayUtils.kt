package io.klaytn.finder.infra.utils

import java.math.BigDecimal
import java.math.BigInteger

class KlayUtils {
    companion object {
        private val HEX_PREFIX = "0x"

        fun pebToKlay(peb: String?): BigDecimal =
                peb?.let {
                    val value =
                            if (isHexPrefixed(it)) BigInteger(it.substring(2), 16).toBigDecimal()
                            else BigDecimal(it)
                    value.divide(BigDecimal(10).pow(18))
                }
                        ?: BigDecimal.ZERO

        fun klayToPeb(klay: BigDecimal?) =
                klay?.let {
                    HEX_PREFIX + it.multiply(BigDecimal(10).pow(18)).toBigInteger().toString(16)
                }

        fun stripHexPrefix(source: String) =
                if (isHexPrefixed(source)) {
                    source.substring(2)
                } else {
                    source
                }

        fun isHexPrefixed(source: String) = source.startsWith(HEX_PREFIX, true)
    }
}
