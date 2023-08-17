package io.klaytn.finder.compiler.service

import io.klaytn.commons.utils.logback.logger
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import java.io.File

class SolidityDownloaderFromS3(
    private val s3Client: S3Client,
    private val s3bucket: String,
    private val parentDir: File,
) : SolidityDownloader {
    private val logger = logger(javaClass)

    override fun getList(os: SolidityDownloader.Os): List<Build> {
        val request = ListObjectsV2Request.builder().bucket(s3bucket).prefix("compiler/${os.path}").build()
        val response = s3Client.listObjectsV2Paginator(request)

        val files = mutableListOf<Build>()
        for (page in response) {
            page.contents().filterNot { it.key().equals("${os.path}/") }.forEach {
                val path = it.key().substringAfter("${os.path}/")
                if(path.isNotBlank()) {
                    val version = path.substringAfter("solc-${os.path}-v").substringBefore("+commit")
                    val build = path.substringAfter("$version+")
                    val longVersion = "$version+$build"
                    files.add(Build(path, version, build, longVersion))
                }
            }
        }
        return files
    }

    override fun download(os: SolidityDownloader.Os, build: Build): File {
        parentDir.mkdirs()

        val file = File(parentDir, build.path)
        if(file.exists()) {
            logger.info("${build.path} => already downloaded...")
        } else {
            try {
                val request = GetObjectRequest.builder().bucket(s3bucket).key("compiler/${os.path}/${build.path}").build()
                s3Client.getObject(request, ResponseTransformer.toFile(file))
                logger.info("${build.path} => downloaded...")
            } catch (exception: Exception) {
                logger.error("${build.path} => fail to download.", exception)
            }

        }

        return file.also {
            it.setReadable(true)
            it.setWritable(true)
            it.setExecutable(true)
        }
    }
}