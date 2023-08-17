package io.klaytn.finder.domain.mysql.set3.nft

import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.domain.mysql.BaseEntity
import io.klaytn.finder.infra.db.jpa.Hex66StringToBigIntegerConverter
import java.math.BigInteger
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "nft_items")
data class NftItem(
    @Column(columnDefinition = "TINYINT")
    val contractType: ContractType,

    @Column
    val contractAddress: String,

    @Column
    val tokenId: String,

    @Column(columnDefinition = "MEDIUMTEXT")
    val tokenUri: String,

    @Column
    var tokenUriUpdatedAt: LocalDateTime? = null,

    @Column
    @Convert(converter = Hex66StringToBigIntegerConverter::class)
    var totalSupply: BigInteger?,

    @Column
    val totalTransfer: Long,

    @Column
    @Convert(converter = Hex66StringToBigIntegerConverter::class)
    val burnAmount: BigInteger?,

    @Column
    val totalBurn: Long,
) : BaseEntity()
