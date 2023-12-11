package io.klaytn.finder.compiler.infra.boot.listener

import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.compiler.service.SolidityBuildFileManager
import io.klaytn.finder.compiler.service.SolidityDownloader
import io.klaytn.finder.compiler.service.SolidityDownloaderFromGcs
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.stereotype.Component

    /**
 * Synchronizes the list stored in S3 before the service starts up.
 */
@Component
class SolidityCompilerSyncWithGcsEventListener(
    val solidityDownloaderFromGcs: SolidityDownloaderFromGcs,
    val solidityBuildFileManager: SolidityBuildFileManager
) {
    private val logger = logger(this.javaClass)

    @org.springframework.context.event.EventListener
    fun onApplicationEvent(event: ApplicationStartedEvent) {

        logger.info("[start] checking and download from s3")
        val os = SolidityDownloader.Os.check() ?: return
        solidityDownloaderFromGcs.getList(os).filter { !solidityBuildFileManager.contains(it) }.forEach { build ->
            solidityDownloaderFromGcs.download(os, build).let {
                solidityBuildFileManager.set(build, it)
            }
        }
        logger.info("[ end] checking and download from s3")

    }
}