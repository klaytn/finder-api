package io.klaytn.finder.infra.db.jpa

import java.math.BigDecimal
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class StringToBigDecimalConverter : AttributeConverter<BigDecimal, String> {
    override fun convertToDatabaseColumn(value: BigDecimal?) = value?.toString()
    override fun convertToEntityAttribute(value: String?) = value?.let { BigDecimal(value) }
}
