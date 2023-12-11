package io.klaytn.finder.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration @EnableConfigurationProperties(FinderGcsProperties::class) class GcpStorageConfig

@ConstructorBinding
@ConfigurationProperties(prefix = "finder.gcp.storage")
data class FinderGcsProperties(
        val privateBucket: String,
)
