package io.klaytn.finder.domain.common

import javax.persistence.AttributeConverter
import javax.persistence.Converter

enum class KaiaUserEmailAuthType(val value: Int) {
    SIGNUP(0),
    EMAIL_CHANGE(1);

    companion object {
        fun of(value: Int) = values().first { it.value == value }
        fun of(value: String) = values().first { it.name.equals(value, ignoreCase = true) }
    }
}

enum class KaiaUserEmailAuthVerificationType(val value: Int) {
    UNVERIFIED(0),
    VERIFIED(1);

    companion object {
        fun of(value: Int) = values().first { it.value == value }
        fun of(value: String) = values().first { it.name.equals(value, ignoreCase = true) }
    }
}

@Converter(autoApply = true)
class KaiaUserEmailAuthTypeAttributeConverter :
    AttributeConverter<KaiaUserEmailAuthType, Int> {
    override fun convertToDatabaseColumn(attribute: KaiaUserEmailAuthType?) =
        attribute?.value

    override fun convertToEntityAttribute(dbData: Int?): KaiaUserEmailAuthType? =
        dbData?.let { KaiaUserEmailAuthType.of(it) }
}

@Converter(autoApply = true)
class KaiaUserEmailAuthVerificationTypeAttributeConverter :
    AttributeConverter<KaiaUserEmailAuthVerificationType, Int> {
    override fun convertToDatabaseColumn(attribute: KaiaUserEmailAuthVerificationType?) =
        attribute?.value

    override fun convertToEntityAttribute(dbData: Int?): KaiaUserEmailAuthVerificationType? =
        dbData?.let { KaiaUserEmailAuthVerificationType.of(it) }
}
