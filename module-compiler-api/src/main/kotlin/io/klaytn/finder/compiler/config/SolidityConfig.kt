package io.klaytn.finder.compiler.config

import com.google.cloud.storage.Storage
import io.klaytn.finder.compiler.service.SolidityBuildFileManager
import io.klaytn.finder.compiler.service.SolidityCompiler
import io.klaytn.finder.compiler.service.SolidityDownloaderFromGit
import io.klaytn.finder.compiler.service.SolidityDownloaderFromGcs
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import java.io.File

@Configuration
class SolidityConfig {
    @Bean
    fun solidityBuildFileManager() = SolidityBuildFileManager()

    @Bean
    fun solidityCompiler(solidityBuildFileManager: SolidityBuildFileManager) =
        SolidityCompiler(solidityBuildFileManager)

    @Bean
    fun solidityDownloaderFromGcs(gcsClient: Storage, solidityProperties: SolidityProperties) =
        SolidityDownloaderFromGcs(
            gcsClient = gcsClient,
            gcsBucket = solidityProperties.compiler.gcsBucket,
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
        val gcsBucket: String,
        val rootPath: File,
    )
}
