package io.klaytn.finder.domain.common

import javax.persistence.AttributeConverter
import javax.persistence.Converter

enum class KaiaUserType(val value: Int) {
    UNVERIFIED(0),
    ACTIVE(1),
    DEACTIVATED(9);

    companion object {
        fun of(value: Int) = values().first { it.value == value }
        fun of(value: String) = values().first { it.name.equals(value, ignoreCase = true) }
    }
}

@Converter(autoApply = true)
class KaiaUserTypeAttributeConverter :
    AttributeConverter<KaiaUserType, Int> {
    override fun convertToDatabaseColumn(attribute: KaiaUserType?) =
        attribute?.value

    override fun convertToEntityAttribute(dbData: Int?): KaiaUserType? =
        dbData?.let { KaiaUserType.of(it) }
}
