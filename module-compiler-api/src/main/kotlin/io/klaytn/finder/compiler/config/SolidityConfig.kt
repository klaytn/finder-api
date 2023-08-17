package io.klaytn.finder.compiler.config

import io.klaytn.finder.compiler.service.SolidityBuildFileManager
import io.klaytn.finder.compiler.service.SolidityCompiler
import io.klaytn.finder.compiler.service.SolidityDownloaderFromGit
import io.klaytn.finder.compiler.service.SolidityDownloaderFromS3
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import software.amazon.awssdk.services.s3.S3Client
import java.io.File

@Configuration
class SolidityConfig {
    @Bean
    fun solidityBuildFileManager() = SolidityBuildFileManager()

    @Bean
    fun solidityCompiler(solidityBuildFileManager: SolidityBuildFileManager) =
        SolidityCompiler(solidityBuildFileManager)

    @Bean
    fun solidityDownloaderFromS3(s3Client: S3Client, solidityProperties: SolidityProperties) =
        SolidityDownloaderFromS3(
            s3Client = s3Client,
            s3bucket = solidityProperties.compiler.s3Bucket,
            parentDir = solidityProperties.compiler.rootPath)

    @Bean
    fun solidityDownloaderFromGit(solidityProperties: SolidityProperties) =
        SolidityDownloaderFromGit(
            restTemplate = RestTemplate(),
            parentDir = solidityProperties.compiler.rootPath)
}

@ConstructorBinding
@ConfigurationProperties(prefix = "finder.contract.solidity")
data class SolidityProperties(
    val compiler: CompilerProperty,
) {
    data class CompilerProperty(
        val s3Bucket: String,
        val rootPath: File,
    )
}
