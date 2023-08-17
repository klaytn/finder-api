package io.klaytn.finder.domain.common

import javax.persistence.AttributeConverter
import javax.persistence.Converter

enum class WalletType(val value: Int) {
    KAIKAS(0),
    METAMASK(1),
    KLIP(2),
    KAS(3),
    ;

    companion object {
        fun of(value: Int) = values().first { it.value == value }
        fun getOrDefaultIfNull(walletType: WalletType?) = walletType ?: KAIKAS
    }
}

@Converter(autoApply = true)
class WalletTypeAttributeConverter : AttributeConverter<WalletType, Int> {
    override fun convertToDatabaseColumn(attribute: WalletType?) = attribute?.value

    override fun convertToEntityAttribute(dbData: Int?): WalletType? =
            dbData?.let { WalletType.of(it) }
}
