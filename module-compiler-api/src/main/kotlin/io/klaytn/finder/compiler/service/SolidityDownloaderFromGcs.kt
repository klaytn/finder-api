package io.klaytn.finder.compiler.service

import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobListOption
import io.klaytn.commons.utils.logback.logger

import java.io.File

class SolidityDownloaderFromGcs(
    private val gcsClient: Storage,
    private val gcsBucket: String,
    private val parentDir: File,
) : SolidityDownloader {
    private val logger = logger(javaClass)

    override fun getList(os: SolidityDownloader.Os): List<Build> {
        val files = mutableListOf<Build>()
        val blobs = gcsClient.list(gcsBucket, BlobListOption.prefix("compiler/${os.path}")).values
        for(blob in blobs) {
            val path = blob.name.substringAfter("${os.path}/")
            if(path.isNotBlank()) {
                val version = path.substringAfter("solc-${os.path}-v").substringBefore("+commit")
                val build = path.substringAfter("$version+")
                val longVersion = "$version+$build"
                files.add(Build(path, version, build, longVersion))
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
                val blob = gcsClient.get(gcsBucket, "compiler/${os.path}/${build.path}")
                blob.downloadTo(file.toPath())
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