package io.klaytn.finder.infra.utils

import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class DateUtils {
    companion object {
        fun from(timestamp: Int) = Date(timestamp * 1000L)

        fun timestampToLocalDateTime(timestamp: Int): LocalDateTime =
                Instant.ofEpochMilli(timestamp * 1000L)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()

        fun dateToLocalDateTime(date: Date): LocalDateTime =
                Instant.ofEpochMilli(date.time).atZone(ZoneId.systemDefault()).toLocalDateTime()

        fun localDateTimeToTimestamp(localDateTime: LocalDateTime): Long =
                Timestamp.valueOf(localDateTime).time / 1000

        fun localDateTimeToEpochMilli(localDateTime: LocalDateTime): Long =
                localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        fun localDateTimeToDate(localDateTime: LocalDateTime): Date =
                Date(localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())

        fun toUTCDateString(localDateTime: LocalDateTime, formatter: DateTimeFormatter): String =
                localDateTime
                        .atZone(ZoneId.systemDefault())
                        .withZoneSameInstant(ZoneId.of("UTC"))
                        .format(formatter)
    }
}
