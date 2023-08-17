package io.klaytn.finder.domain.mysql.set3.nft

import io.klaytn.finder.domain.common.AccountAddress
import io.klaytn.finder.domain.common.AccountAddressAttributeConverter
import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.domain.mysql.BaseEntity
import io.klaytn.finder.infra.db.DbTableConstants
import io.klaytn.finder.infra.db.jpa.Hex66StringToBigIntegerConverter
import java.math.BigInteger
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = DbTableConstants.nftBurns)
data class NftBurn(
    @Column(columnDefinition = "TINYINT")
    val contractType: ContractType,

    @Column
    val contractAddress: String,

    @Column
    @Convert(converter = AccountAddressAttributeConverter::class)
    val from: AccountAddress,

    @Column
    @Convert(converter = AccountAddressAttributeConverter::class)
    val to: AccountAddress?,

    @Column
    @Convert(converter = Hex66StringToBigIntegerConverter::class)
    val tokenCount: BigInteger,

    @Column
    val tokenId: String,

    @Column
    val timestamp: Int,

    @Column
    val blockNumber: Long,

    @Column
    val transactionHash: String,

    @Column
    val displayOrder: String,
) : BaseEntity()
