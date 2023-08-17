package io.klaytn.commons.aws.autoconfigure

import java.time.Duration
import java.time.temporal.ChronoUnit
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.convert.DurationUnit
import software.amazon.awssdk.regions.Region

@ConstructorBinding
@ConfigurationProperties(prefix = "aws")
data class AwsProperties(
        val enabled: Boolean,
        val credentials: AwsCredentialsProperties?,
        val s3: AwsS3Properties?,
        val ses: AwsSesProperties?,
        val secretsManager: AwsSecretsManagerProperties?,
)

data class AwsCredentialsProperties(
        val accessKey: String,
        val secretKey: String,
)

class AwsS3Properties(
        region: Region,
) : AwsPropertiesBase(region)

class AwsSesProperties(
        region: Region,
) : AwsPropertiesBase(region)

class AwsSecretsManagerProperties(
        region: Region,
        val arn: String,
) : AwsPropertiesBase(region)

open class AwsPropertiesBase(
        val region: Region,
        val httpClient: AwsHttpClientProperties? = null,
)

data class AwsHttpClientProperties(
        val maxConnections: Int,
        @DurationUnit(ChronoUnit.SECONDS) val connectionTimeoutSeconds: Duration,
        @DurationUnit(ChronoUnit.SECONDS) val socketTimeoutSeconds: Duration,
)
