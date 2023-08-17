package io.klaytn.finder.infra.db.jpa

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class JsonStringToMapListConverter : AttributeConverter<List<Map<String, String>>, String> {
    private val objectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(values: List<Map<String, String>>?) =
            values?.let { objectMapper.writeValueAsString(it) }

    override fun convertToEntityAttribute(jsonString: String?) =
            jsonString?.let {
                objectMapper.readValue(
                        it.toByteArray(),
                        object : TypeReference<List<Map<String, String>>>() {}
                )
            }
                    ?: emptyList()
}
