package io.klaytn.commons.gcp.autoconfigure

import io.klaytn.commons.utils.logback.logger
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.cloud.gcp.core.GcpProjectIdProvider
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions


@Configuration
@EnableConfigurationProperties(GcpProperties::class)
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
}
