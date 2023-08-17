package io.klaytn.finder.infra.db.jpa

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class JsonStringToStringListConverter : AttributeConverter<List<String>, String> {
    private val objectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(values: List<String>?) =
            values?.let { objectMapper.writeValueAsString(it) }

    override fun convertToEntityAttribute(jsonString: String?) =
            jsonString?.let {
                objectMapper.readValue(it.toByteArray(), object : TypeReference<List<String>>() {})
            }
                    ?: emptyList()
}
