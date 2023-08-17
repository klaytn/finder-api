package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.domain.mysql.BaseEntity
import io.klaytn.finder.infra.db.jpa.Hex66StringToBigIntegerConverter
import io.klaytn.finder.infra.db.jpa.StringToBigDecimalConverter
import java.math.BigDecimal
import java.math.BigInteger
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "contracts")
data class Contract(
    @Column
    val contractAddress: String,

    @Column(columnDefinition = "TINYINT")
    var contractType: ContractType,

    @Column
    var name: String?,

    @Column
    var symbol: String?,

    @Column(columnDefinition = "TEXT")
    var icon: String?,

    @Column
    var officialSite: String?,

    @Column
    var officialEmailAddress: String?,

    @Column
    var decimal: Int,

    @Column
    val holderCount: Long,

    @Column
    @Convert(converter = StringToBigDecimalConverter::class)
    var totalSupply: BigDecimal,

    @Column
    val totalTransfer: Long,

    @Column(columnDefinition = "TINYINT")
    var verified: Boolean,

    @Column(columnDefinition = "TINYINT")
    val txError: Boolean,

    @Column
    val implementationAddress: String?,

    @Column
    @Convert(converter = Hex66StringToBigIntegerConverter::class)
    val burnAmount: BigInteger?,

    @Column
    val totalBurn: Long?,

    @Column
    val totalSupplyOrder: String?,
) : BaseEntity()
