package io.klaytn.finder.domain.common

import javax.persistence.AttributeConverter
import javax.persistence.Converter

enum class ContractType(val value: Int) {
    ERC20(0),
    KIP7(1),
    KIP17(2),
    KIP37(3),
    ERC721(4),
    ERC1155(5),
    CONSENSUS_NODE(126),
    CUSTOM(127);

    companion object {
        fun of(value: Int) = values().first { it.value == value }
        fun getTokenTypes() = setOf(ERC20, KIP7)
        fun getNftTypes() = setOf(KIP17, KIP37, ERC721, ERC1155)
        fun of(name: String) = values().first { it.name.equals(name, true) }
    }
}

@Converter(autoApply = true)
class ContractTypeAttributeConverter : AttributeConverter<ContractType, Int> {
    override fun convertToDatabaseColumn(attribute: ContractType?) = attribute?.value

    override fun convertToEntityAttribute(dbData: Int?): ContractType? =
            dbData?.let { ContractType.of(it) }
}
