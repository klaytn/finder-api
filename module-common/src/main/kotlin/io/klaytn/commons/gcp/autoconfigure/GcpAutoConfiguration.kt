package io.klaytn.commons.aws.autoconfigure

import io.klaytn.commons.utils.logback.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.http.apache.ApacheHttpClient
import org.springframework.cloud.gcp.autoconfigure.core.GcpContextAutoConfiguration
import org.springframework.cloud.gcp.autoconfigure.core.GcpProperties
import org.springframework.cloud.gcp.autoconfigure.config.GcpConfigProperties
import org.springframework.cloud.gcp.core.GcpProjectIdProvider
import org.springframework.cloud.gcp.storage.GoogleStorageResource
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions


@Configuration
@EnableConfigurationProperties(AwsProperties::class)
@ConditionalOnProperty(value = ["gcp.enabled"], havingValue = "true")
open class GcpAutoConfiguration {
    private val logger = logger(GcpAutoConfiguration::class.java)

    open fun gcpProjectIdProvider(gcpProperties: GcpProperties): GcpProjectIdProvider {
        return GcpProjectIdProvider { gcpProperties.projectId }
    }

    open fun gcsClient(gcpProperties: GcpProperties): Storage {
        return StorageOptions.newBuilder()
            .setProjectId(gcpProperties.projectId)
            .build()
            .service
    }
    // TODO: async client
    open fun gcsAsyncClient(gcpProperties: GcpProperties): Storage {
        return StorageOptions.newBuilder()
            .setProjectId(gcpProperties.projectId)
            .build()
            .service
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
