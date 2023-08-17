package io.klaytn.finder.domain.mysql.set3.token

import io.klaytn.finder.domain.common.AccountAddress
import io.klaytn.finder.domain.common.AccountAddressAttributeConverter
import io.klaytn.finder.domain.mysql.BaseEntity
import io.klaytn.finder.infra.db.DbTableConstants
import io.klaytn.finder.infra.db.jpa.Hex66StringToBigIntegerConverter
import java.math.BigInteger
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = DbTableConstants.tokenTransfers)
data class TokenTransfer(
    @Column
    val transactionHash: String,

    @Column
    val blockNumber: Long,

    @Column
    val timestamp: Int,

    @Column
    @Convert(converter = AccountAddressAttributeConverter::class)
    val from: AccountAddress,

    @Column
    @Convert(converter = AccountAddressAttributeConverter::class)
    val to: AccountAddress?,

    @Column
    val contractAddress: String,

    @Column
    @Convert(converter = Hex66StringToBigIntegerConverter::class)
    val amount: BigInteger,

    @Column
    val displayOrder: String,
) : BaseEntity()
