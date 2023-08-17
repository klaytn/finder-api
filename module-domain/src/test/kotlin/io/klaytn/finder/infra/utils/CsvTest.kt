package io.klaytn.finder.infra.utils

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import java.io.FileWriter
import java.math.BigDecimal
import java.util.*
import org.junit.jupiter.api.Test

class CsvTest {
    @Test
    fun write() {
        val csvMapper =
                CsvMapper.builder().addModule(kotlinModule()).addModule(JavaTimeModule()).build()
        val data =
                listOf(
                        BlockCSVListView(85084265, Date(), 6, BigDecimal(9.6), 10000),
                        BlockCSVListView(85084265, Date(), 6, BigDecimal(9.6), 10000),
                )
        FileWriter("/tmp/test.cvs").use { writer ->
            csvMapper
                    .writer(csvMapper.schemaFor(BlockCSVListView::class.java).withHeader())
                    .writeValues(writer)
                    .writeAll(data)
                    .close()
        }
    }
}

@JsonPropertyOrder(
        value = ["blockId", "datetime", "totalTransactionCount", "rewardKlay", "blockSize"]
)
data class BlockCSVListView(
        @JsonProperty("BLOCK #") val blockId: Long,
        @JsonProperty("TIME(KST)")
        @JsonFormat(
                shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd'T'HH:mm:ssZ",
                timezone = "Asia/Seoul"
        )
        val datetime: Date,
        @JsonProperty("TOTAL TXS") val totalTransactionCount: Long,
        @JsonProperty("REWARD(KLAY)") val rewardKlay: BigDecimal,
        @JsonProperty("SIZE(BYTE)") val blockSize: Long,
)
