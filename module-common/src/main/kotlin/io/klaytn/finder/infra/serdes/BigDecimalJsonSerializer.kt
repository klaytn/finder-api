package io.klaytn.finder.infra.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.math.BigDecimal
import org.springframework.boot.jackson.JsonComponent

@JsonComponent
class BigDecimalJsonSerializer : JsonSerializer<BigDecimal>() {
    override fun serialize(value: BigDecimal?, jg: JsonGenerator, sp: SerializerProvider?) {
        if (value == null) {
            jg.writeNull()
            return
        }

        jg.writeString(value.stripTrailingZeros().toPlainString())
    }
}
