package io.klaytn.commons.aws.secretsmanager

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.klaytn.commons.aws.autoconfigure.AwsAutoConfiguration
import io.klaytn.commons.aws.autoconfigure.AwsHttpClientProperties
import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest
import java.time.Duration

class SecretManagerPropertyProcessor : EnvironmentPostProcessor {
    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication?) {
        if ("true" != environment.getProperty("aws.enabled", "true")) {
            return
        }

        val credentialsProvider = createCredentialsProvider(environment)
        val httpClientProperties = createHttpClientProperties(environment)

        val region = environment.getProperty("aws.secrets-manager.region")
        val arn = environment.getProperty("aws.secrets-manager.arn")

        if (region.isNullOrBlank() || arn.isNullOrBlank()) {
            return
        }

        val secretsManagerClient = SecretsManagerClient.builder()
            .credentialsProvider(credentialsProvider)
            .httpClientBuilder(AwsAutoConfiguration.createHttpClientBuilder(httpClientProperties))
            .region(Region.of(region))
            .build()

        val propertiesPropertySource = createPropertiesPropertySource(secretsManagerClient, arn)
        environment.propertySources.addFirst(propertiesPropertySource)
    }

    private fun createPropertiesPropertySource(client: SecretsManagerClient, arn: String): MapPropertySource {
        val request = GetSecretValueRequest.builder().secretId(arn).build()
        val response = client.getSecretValue(request)

        val secretValue: Map<String, Any> = jacksonObjectMapper().readValue(response.secretString())
        return MapPropertySource("secrets-manager", secretValue)
    }

    private fun createCredentialsProvider(environment: ConfigurableEnvironment): AwsCredentialsProvider? {
        val prefix = "aws.credentials"
        val accessKey = environment.getProperty("$prefix.access-key")
        val secretKey = environment.getProperty("$prefix.secret-key")

        if (accessKey.isNullOrBlank() || secretKey.isNullOrBlank()) {
            return null
        }

        val awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey)

        return StaticCredentialsProvider.create(awsBasicCredentials)
    }

    private fun createHttpClientProperties(environment: ConfigurableEnvironment): AwsHttpClientProperties? {
        val prefix = "aws.secrets-manager.http-client"

        val maxConnections = environment.getProperty("$prefix.max-connections", Int::class.java)
        val connectionTimeout = environment.getProperty("$prefix.connection-timeout-seconds", Duration::class.java)
        val socketTimeout = environment.getProperty("$prefix.socket-timeout-seconds", Duration::class.java)

        if (maxConnections == null || connectionTimeout == null || socketTimeout == null) {
            return null
        }

        return AwsHttpClientProperties(maxConnections, connectionTimeout, socketTimeout)
    }
}
