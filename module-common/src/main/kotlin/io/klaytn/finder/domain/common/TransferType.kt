package io.klaytn.finder.domain.common

import javax.persistence.AttributeConverter
import javax.persistence.Converter

enum class TransferType(val value: Int) {
    TOKEN(0),
    NFT(1),
    ;

    companion object {
        fun of(value: Int) = TransferType.values().first { it.value == value }
    }
}

@Converter(autoApply = true)
class TransferTypeAttributeConverter : AttributeConverter<TransferType, Int> {
    override fun convertToDatabaseColumn(attribute: TransferType?) = attribute?.value
    override fun convertToEntityAttribute(dbData: Int?): TransferType? =
            dbData?.let { TransferType.of(it) }
}
