package io.klaytn.finder.infra.db.jpa

import io.klaytn.finder.infra.utils.KlayUtils
import java.math.BigDecimal
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class PebToKlayConverter : AttributeConverter<BigDecimal, String> {
    override fun convertToDatabaseColumn(value: BigDecimal?) = value?.let { KlayUtils.klayToPeb(it) }
    override fun convertToEntityAttribute(value: String?) = KlayUtils.pebToKlay(value)
}
