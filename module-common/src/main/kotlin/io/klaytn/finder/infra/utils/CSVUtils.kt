package io.klaytn.finder.infra.utils

import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.SequenceWriter
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import java.io.OutputStream

class CSVUtils {
    companion object {
        private val csvMapper =
                CsvMapper.builder().addModule(kotlinModule()).addModule(JavaTimeModule()).build()

        fun <T> objectWriter(pojoType: Class<T>): ObjectWriter =
                csvMapper.writer(csvMapper.schemaFor(pojoType).withHeader())

        fun streamWriter(objectWriter: ObjectWriter, outputStream: OutputStream): SequenceWriter =
                objectWriter.writeValues(outputStream)

        fun <T> streamWriter(pojoType: Class<T>, outputStream: OutputStream): SequenceWriter {
            val objectWriter = objectWriter(pojoType)
            return streamWriter(objectWriter, outputStream)
        }
    }
}
