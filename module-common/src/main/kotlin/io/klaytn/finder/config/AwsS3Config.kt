package io.klaytn.finder.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration @EnableConfigurationProperties(FinderS3Properties::class) class AwsS3Config

@ConstructorBinding
@ConfigurationProperties(prefix = "finder.aws.s3")
data class FinderS3Properties(
        val privateBucket: String,
)
