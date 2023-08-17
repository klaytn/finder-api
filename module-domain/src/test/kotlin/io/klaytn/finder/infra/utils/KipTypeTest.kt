package io.klaytn.finder.infra.utils

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.klaytn.caver.abi.ABI
import io.klaytn.finder.domain.common.KipEvent
import io.klaytn.finder.domain.common.KipType
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource

class KipTypeTest {
    @Test
    fun event() {
        val events = KipType.values().map { it.getEvents() }.flatten()
        val eventLogs: List<KipEventLog> = jacksonObjectMapper().readValue(ClassPathResource("/data/event_logs.json").inputStream)

        eventLogs.forEach { process(events.groupBy { event -> event.signature }, it.topics, it.data) }
    }

    private fun process(events: Map<String, List<KipEvent>>, topics: List<String>, data: String) {
        if (topics.isEmpty()) {
            return
        }

        val event = events[topics[0]]?.find { it.indexedParams.size == topics.size - 1 } ?: return

        var index = 1

        if (event.name != "Transfer") {
            return
        }

        println(event.name + " (" + event.signature + ")")

        event.indexedParams.forEach { name, type ->
            val parameter = ABI.decodeParameter(type, topics[index])
            println("[I] " + name + ": " + parameter.typeAsString + " => " + parameter.value)
            index++
        }

        if (event.nonIndexedParams.isEmpty()) {
            println()
            return
        }

        val nonIndexed = ABI.decodeParameters(event.nonIndexedParams.values.toList(), data)

        index = 0

        event.nonIndexedParams.forEach { name, _ ->
            println("[N] " + name + ": " + nonIndexed[index].typeAsString + " => " + nonIndexed[index].value)
        }

        println()
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class KipEventLog(
        val data: String,
        val topics: List<String>
    )
}
