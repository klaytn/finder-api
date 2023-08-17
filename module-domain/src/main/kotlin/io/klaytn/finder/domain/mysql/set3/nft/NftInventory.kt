package io.klaytn.finder.domain.mysql.set3.nft

import io.klaytn.finder.domain.common.AccountAddress
import io.klaytn.finder.domain.common.AccountAddressAttributeConverter
import io.klaytn.finder.domain.common.ContractType
import io.klaytn.finder.domain.mysql.BaseEntity
import io.klaytn.finder.infra.db.jpa.Hex66StringToBigIntegerConverter
import java.math.BigInteger
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "nft_inventories")
data class NftInventory(
    @Column(columnDefinition = "TINYINT")
    val contractType: ContractType,

    @Column
    val contractAddress: String,

    @Column(name = "holder_address")
    @Convert(converter = AccountAddressAttributeConverter::class)
    val holderAddress: AccountAddress,

    @Column
    val tokenId: String,

    @Column
    val tokenUri: String,

    @Column
    @Convert(converter = Hex66StringToBigIntegerConverter::class)
    val tokenCount: BigInteger,

    @Column
    val lastTransactionTime: Int,
) : BaseEntity()
