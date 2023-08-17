package io.klaytn.finder.domain.mysql.set1

import io.klaytn.finder.domain.mysql.BaseEntity
import io.klaytn.finder.infra.db.jpa.Hex66StringToBigIntegerConverter
import java.math.BigInteger
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "block_rewards")
data class BlockReward(
    @Column
    val number: Long,

    @Column
    @Convert(converter = Hex66StringToBigIntegerConverter::class)
    val minted: BigInteger,

    @Column
    @Convert(converter = Hex66StringToBigIntegerConverter::class)
    val totalFee: BigInteger,

    @Column
    @Convert(converter = Hex66StringToBigIntegerConverter::class)
    val burntFee: BigInteger,

    @Column
    @Convert(converter = Hex66StringToBigIntegerConverter::class)
    val proposer: BigInteger,

    @Column
    @Convert(converter = Hex66StringToBigIntegerConverter::class)
    val stakers: BigInteger,

    @Column
    @Convert(converter = Hex66StringToBigIntegerConverter::class)
    val kgf: BigInteger,

    @Column
    @Convert(converter = Hex66StringToBigIntegerConverter::class)
    val kir: BigInteger,

    @Column(columnDefinition = "TEXT")
    val rewards: String,
) : BaseEntity()
