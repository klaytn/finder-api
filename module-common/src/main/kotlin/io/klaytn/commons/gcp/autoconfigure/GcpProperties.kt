package io.klaytn.commons.gcp.autoconfigure

import java.time.Duration
import java.time.temporal.ChronoUnit
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.convert.DurationUnit
import software.amazon.awssdk.regions.Region

@ConstructorBinding
@ConfigurationProperties(prefix = "gcp")
data class GcpProperties(
        val enabled: Boolean,
        val credentials: GcpCredentialsProperties?,
        val s3: GcpS3Properties?,
        val ses: GcpSesProperties?,
        val secretsManager: GcpSecretsManagerProperties?,
)

data class GcpCredentialsProperties(
        val accessKey: String,
        val secretKey: String,
)

class GcpS3Properties(
        region: Region,
) : GcpPropertiesBase(region)

class GcpSesProperties(
        region: Region,
) : GcpPropertiesBase(region)

class GcpSecretsManagerProperties(
        region: Region,
        val arn: String,
) : GcpPropertiesBase(region)

open class GcpPropertiesBase(
        val region: Region,
        val httpClient: GcpHttpClientProperties? = null,
)

data class GcpHttpClientProperties(
        val maxConnections: Int,
        @DurationUnit(ChronoUnit.SECONDS) val connectionTimeoutSeconds: Duration,
        @DurationUnit(ChronoUnit.SECONDS) val socketTimeoutSeconds: Duration,
)
