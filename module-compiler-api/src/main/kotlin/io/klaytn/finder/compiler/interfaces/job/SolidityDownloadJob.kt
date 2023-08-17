package io.klaytn.finder.compiler.interfaces.job

import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.compiler.service.SolidityBuildFileManager
import io.klaytn.finder.compiler.service.SolidityDownloader
import io.klaytn.finder.compiler.service.SolidityDownloaderFromGit
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SolidityDownloadJob(
    private val solidityDownloaderFromGit: SolidityDownloaderFromGit,
    private val solidityBuildFileManager: SolidityBuildFileManager,
) {
    private val logger = logger(this.javaClass)

    @Scheduled(initialDelay = 30 * 1000L, fixedRate = 60 * 60 * 1000L)
    fun download() {
        val os = SolidityDownloader.Os.check() ?: return

        logger.info("[start] checking and download from git")
        solidityDownloaderFromGit.getList(os).filter { !solidityBuildFileManager.contains(it) }.forEach { build ->
            solidityDownloaderFromGit.download(os, build)?.let {
                solidityBuildFileManager.set(build, it)
            }
        }
        logger.info("[ end] checking and download from git")
    }
}