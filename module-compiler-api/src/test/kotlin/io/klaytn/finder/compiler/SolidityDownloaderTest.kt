package io.klaytn.finder.compiler

import io.klaytn.finder.compiler.service.SolidityDownloader
import io.klaytn.finder.compiler.service.SolidityDownloaderFromGit
import io.klaytn.finder.compiler.service.SolidityDownloaderFromS3
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestTemplate
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
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
        val s3client = S3Client.builder().region(Region.AP_NORTHEAST_2).build()
        val downloader = SolidityDownloaderFromS3(s3client, "AWS_S3_PRIVATE_BUCKET", File("/tmp/solidity"))
        downloader.getList(os).forEach {
            downloader.download(os, it)
        }
    }
}
