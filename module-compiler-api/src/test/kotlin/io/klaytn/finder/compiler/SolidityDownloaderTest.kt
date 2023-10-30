package io.klaytn.finder.compiler

import io.klaytn.finder.compiler.service.SolidityDownloader
import io.klaytn.finder.compiler.service.SolidityDownloaderFromGcs
import io.klaytn.finder.compiler.service.SolidityDownloaderFromGit
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestTemplate
import software.amazon.awssdk.regions.Region
import com.google.cloud.storage.StorageOptions

import java.io.File

class SolidityDownloaderTest {
    @Test
    fun download() {
        val os = SolidityDownloader.Os.check() ?: return
        val downloader = SolidityDownloaderFromGit(RestTemplate(), File("/tmp/solidity"))
        downloader.getList(os).forEach {
            downloader.download(os, it)
        }
    }

    @Test
    fun downloadWithS3() {
        val os = SolidityDownloader.Os.check() ?: return
        val gcsClient = StorageOptions.newBuilder().setProjectId("klaytn-finder").build().service
        val downloader = SolidityDownloaderFromGcs(gcsClient, "AWS_S3_PRIVATE_BUCKET", File("/tmp/solidity"))
        downloader.getList(os).forEach {
            downloader.download(os, it)
        }
    }
}
