package io.klaytn.finder.domain.common

import javax.persistence.AttributeConverter
import javax.persistence.Converter

enum class GovernanceCouncilContractType(val value: Int) {
    NODE(0),
    STAKING(1),
    REWARD(2);

    companion object {
        fun of(value: Int) = values().first { it.value == value }
        fun of(value: String) = values().first { it.name.equals(value, ignoreCase = true) }
    }
}

@Converter(autoApply = true)
class GovernanceCouncilContractTypeAttributeConverter :
        AttributeConverter<GovernanceCouncilContractType, Int> {
    override fun convertToDatabaseColumn(attribute: GovernanceCouncilContractType?) =
            attribute?.value

    override fun convertToEntityAttribute(dbData: Int?): GovernanceCouncilContractType? =
            dbData?.let { GovernanceCouncilContractType.of(it) }
}
