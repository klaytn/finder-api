package io.klaytn.finder.compiler

import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.compiler.interfaces.job.SolidityDownloadJob
import io.klaytn.finder.compiler.service.SolidityBuildFileManager
import io.klaytn.finder.compiler.service.SolidityCompiler
import io.klaytn.finder.compiler.service.SolidityDownloaderFromGit
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import org.springframework.web.client.RestTemplate
import java.io.File

class SolidityCompilerTest {
    lateinit var solidityCompiler: SolidityCompiler
    private val logger = logger(javaClass)

    @BeforeEach
    fun before() {
        val solidityBuildFileManager = SolidityBuildFileManager()
        solidityCompiler = SolidityCompiler(solidityBuildFileManager)
        val downloader = SolidityDownloaderFromGit(RestTemplate(), File("/tmp/solidity"))

        val solidityDownloadJob = SolidityDownloadJob(downloader, solidityBuildFileManager)
        solidityDownloadJob.download()
    }

    @Test
    fun test() {
        compile("/Treasury.sol", "0.8.9+commit.e5eed63a", true, 200)
    }

    private fun compile(path: String, version: String, optimize: Boolean, optimizeRuns: Long) {
        solidityCompiler.compile(
            ClassPathResource(path).file,
            SolidityCompiler.Option(version = version, optimize = optimize, optimizeRuns = optimizeRuns)
        ).forEachIndexed { index, result ->
            logger.debug("compileResult[$index] : $result")
        }
    }
}
