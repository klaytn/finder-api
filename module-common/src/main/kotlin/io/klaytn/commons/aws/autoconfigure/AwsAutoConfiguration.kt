package io.klaytn.commons.aws.autoconfigure

import io.klaytn.commons.utils.logback.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.http.apache.ApacheHttpClient
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.ses.SesClient

@Configuration
@EnableConfigurationProperties(AwsProperties::class)
@ConditionalOnProperty(value = ["aws.enabled"], havingValue = "true")
open class AwsAutoConfiguration {
    private val logger = logger(AwsAutoConfiguration::class.java)

    @Bean
    @ConditionalOnProperty(prefix = "aws.credentials", name = ["access-key", "secret-key"])
    open fun awsCredentialsProvider(awsProperties: AwsProperties): AwsCredentialsProvider {
        val awsCredentials = awsProperties.credentials!!
        val awsBasicCredentials = AwsBasicCredentials.create(awsCredentials.accessKey, awsCredentials.secretKey)

        return StaticCredentialsProvider.create(awsBasicCredentials).also {
            logger.info("awsCredentialsProvider is created...")
        }
    }

    @Bean
    @ConditionalOnProperty("aws.s3.region")
    open fun awsS3Client(
        awsProperties: AwsProperties,
        @Autowired(required = false) awsCredentialsProvider: AwsCredentialsProvider?,
    ): S3Client {
        val s3ClientProperties = awsProperties.s3!!
        val region = s3ClientProperties.region
        val httpClientBuilder = createHttpClientBuilder(s3ClientProperties.httpClient)

        return S3Client.builder()
            .credentialsProvider(awsCredentialsProvider)
            .httpClientBuilder(httpClientBuilder)
            .region(region)
            .build().also {
                logger.info("awsS3Client is created (region=${s3ClientProperties.region})...")
            }
    }

    @Bean
    @ConditionalOnProperty("aws.s3.region")
    open fun awsS3AsyncClient(
        awsProperties: AwsProperties,
        @Autowired(required = false) awsCredentialsProvider: AwsCredentialsProvider?,
    ): S3AsyncClient {
        val s3ClientProperties = awsProperties.s3!!
        val region = s3ClientProperties.region

        return S3AsyncClient.crtBuilder()
            .credentialsProvider(awsCredentialsProvider)
            .region(region)
            .build().also {
                logger.info("awsS3AsyncClient is created (region=${s3ClientProperties.region})...")
            }
    }

    @Bean
    @ConditionalOnProperty("aws.ses.region")
    open fun awsSesClient(
        awsProperties: AwsProperties,
        @Autowired(required = false) awsCredentialsProvider: AwsCredentialsProvider?,
    ): SesClient {
        val sesClientProperties = awsProperties.ses!!
        val region = sesClientProperties.region
        val httpClientBuilder = createHttpClientBuilder(sesClientProperties.httpClient)

        return SesClient.builder()
            .credentialsProvider(awsCredentialsProvider)
            .httpClientBuilder(httpClientBuilder)
            .region(region)
            .build().also {
                logger.info("awsSesClient is created (region=${sesClientProperties.region})...")
            }
    }

    companion object {
        fun createHttpClientBuilder(httpClientProperties: AwsHttpClientProperties?) = httpClientProperties?.let {
            ApacheHttpClient.builder()
                .maxConnections(it.maxConnections)
                .connectionTimeout(it.connectionTimeoutSeconds)
                .socketTimeout(it.socketTimeoutSeconds)
        }
    }
}
