package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.mysql.BaseEntity
import io.klaytn.finder.infra.db.jpa.Hex66StringToBigIntegerConverter
import java.math.BigInteger
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "block_burns")
data class BlockBurn(
    @Column
    val number: Long,

    @Column
    @Convert(converter = Hex66StringToBigIntegerConverter::class)
    val fees: BigInteger,

    @Column
    @Convert(converter = Hex66StringToBigIntegerConverter::class)
    val accumulateFees: BigInteger,

    @Column
    @Convert(converter = Hex66StringToBigIntegerConverter::class)
    val klay: BigInteger?,

    @Column
    @Convert(converter = Hex66StringToBigIntegerConverter::class)
    val accumulateKlay: BigInteger,

    @Column
    val timestamp: Int,
) : BaseEntity()
