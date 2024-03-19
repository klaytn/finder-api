package io.klaytn.commons.gcp.autoconfigure

import java.time.Duration
import java.time.temporal.ChronoUnit
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.convert.DurationUnit

@ConstructorBinding
@ConfigurationProperties(prefix = "gcp")
data class GcpProperties(
        val enabled: Boolean,
        val projectId: String,
        val region: String,
)
