package io.klaytn.finder.infra.db.jpa

import java.math.BigInteger
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class Hex66StringToBigIntegerConverter : AttributeConverter<BigInteger, String> {
    override fun convertToDatabaseColumn(value: BigInteger?) =
            value?.let { "0x${it.toString(16).padStart(64, '0')}" }

    override fun convertToEntityAttribute(value: String?) =
            value?.let { value.substring(2).toBigInteger(16) }
}
