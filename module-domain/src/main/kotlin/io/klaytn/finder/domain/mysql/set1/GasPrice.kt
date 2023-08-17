package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.mysql.BaseEntity
import io.klaytn.finder.infra.db.jpa.StringToBigDecimalConverter
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "gas_prices")
data class GasPrice(
    @Column
    val minBlockNumber: Long,

    @Column
    val maxBlockNumber: Long,

    @Column
    @Convert(converter = StringToBigDecimalConverter::class)
    val gasPrice: BigDecimal,
) : BaseEntity()