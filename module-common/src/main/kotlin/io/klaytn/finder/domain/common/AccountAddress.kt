package io.klaytn.finder.domain.common

import javax.persistence.AttributeConverter
import javax.persistence.Converter

data class AccountAddress(
        val address: String,
        var accountType: AccountType? = null,
        var contractType: ContractType? = null,
        var contract: AccountAddressContract? = null,
        var knsDomain: String? = null,
        var addressLabel: String? = null
) {
    companion object {
        fun of(address: String) = AccountAddress(address = address)
    }
}

data class AccountAddressContract(
        val symbol: String,
        val name: String,
        val icon: String?,
        val verified: Boolean
)

@Converter(autoApply = true)
class AccountAddressAttributeConverter : AttributeConverter<AccountAddress, String> {
    override fun convertToDatabaseColumn(attribute: AccountAddress?) = attribute?.address

    override fun convertToEntityAttribute(dbData: String?): AccountAddress? =
            dbData?.let { AccountAddress.of(it) }
}
