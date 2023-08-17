package io.klaytn.finder.infra.utils

import java.math.BigDecimal

fun BigDecimal.applyDecimal(decimal: Int) = this.divide(BigDecimal(10).pow(decimal))
