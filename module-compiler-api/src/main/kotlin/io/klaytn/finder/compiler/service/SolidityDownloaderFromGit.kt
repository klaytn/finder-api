package io.klaytn.finder.compiler.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.klaytn.commons.utils.logback.logger
import org.springframework.http.HttpMethod
import org.springframework.util.StreamUtils
import org.springframework.web.client.RestTemplate
import java.io.File
import java.io.FileOutputStream

class SolidityDownloaderFromGit(
    private val restTemplate: RestTemplate,
    private val parentDir: File,
) : SolidityDownloader {
    private val logger = logger(javaClass)

    override fun getList(os: SolidityDownloader.Os): List<Build> {
        val url = "https://raw.githubusercontent.com/ethereum/solc-bin/gh-pages/${os.path}/list.json"

        return restTemplate.getForObject(url, String::class.java)?.let {
            val response: BuildListResponse = jacksonObjectMapper().readValue(it)
            response.builds
        } ?: emptyList()
    }

    override fun download(os: SolidityDownloader.Os, build: Build): File? {
        parentDir.mkdirs()
        val file = File(parentDir, build.path)

        if (file.exists()) {
            return file.also {
                logger.info("${os.path}/${build.path} => already downloaded...")
            }
        }

        val url = "https://github.com/ethereum/solc-bin/raw/gh-pages/${os.path}/${build.path}"

        return restTemplate.execute(url, HttpMethod.GET, null, { response ->
            StreamUtils.copy(response.body, FileOutputStream(file))

            file.also {
                it.setReadable(true)
                it.setWritable(true)
                it.setExecutable(true)
            }
        }).also {
            logger.info("${os.path}/${build.path} => $it")
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class BuildListResponse(
        val builds: List<Build>,
    )
}