package io.klaytn.finder.domain.mysql.set3.token

import io.klaytn.finder.domain.common.AccountAddress
import io.klaytn.finder.domain.common.AccountAddressAttributeConverter
import io.klaytn.finder.domain.mysql.BaseEntity
import io.klaytn.finder.infra.db.jpa.Hex66StringToBigIntegerConverter
import java.math.BigInteger
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "token_holders")
data class TokenHolder(
    @Column
    val contractAddress: String,

    @Column
    @Convert(converter = AccountAddressAttributeConverter::class)
    val holderAddress: AccountAddress,

    @Column
    @Convert(converter = Hex66StringToBigIntegerConverter::class)
    val amount: BigInteger,

    @Column
    val lastTransactionTime: Int,
) : BaseEntity()
