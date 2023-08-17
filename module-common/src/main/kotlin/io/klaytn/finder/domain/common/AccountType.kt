package io.klaytn.finder.domain.common

import javax.persistence.AttributeConverter
import javax.persistence.Converter

enum class AccountType(val value: Int) {
    EOA(0),
    SCA(1);

    companion object {
        fun of(value: Int) = values().first { it.value == value }
    }
}

@Converter(autoApply = true)
class AccountTypeAttributeConverter : AttributeConverter<AccountType, Int> {
    override fun convertToDatabaseColumn(attribute: AccountType?) = attribute?.value

    override fun convertToEntityAttribute(dbData: Int?): AccountType? =
            dbData?.let { AccountType.of(it) }
}
