package io.klaytn.finder.infra.utils

import java.io.File
import java.io.FileReader
import java.util.concurrent.atomic.AtomicInteger
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Test

class TokenImageUploadTest {
    @Test
    fun test() {
        val dryRun = true
        val imagePath = "/Users/marcusmoon/projects/finder-static/token_img"
        val lines = IOUtils.readLines(FileReader("./src/test/resources/image/img_list.csv"))
        println(lines)

        val files = mutableMapOf<String, String>()
        File(imagePath).walk().forEach {
            val filename = it.name
            val symbol = FilenameUtils.getBaseName(filename).substringBefore("(")
            files[symbol.lowercase()] = filename
        }

        val index = AtomicInteger(0)
        val client = OkHttpClient()
        lines.forEach { it ->
            val tokens = it.split(",")

            val symbol = tokens[0].lowercase()
            val contractAddress = tokens[1]
            if (files.containsKey(symbol)) {
                val filename = files[symbol]
                val file = File("$imagePath/${filename}")
                if (!file.exists()) {
                    return
                }

                val requestBody =
                        MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart(
                                        "tokenImage",
                                        filename,
                                        file.asRequestBody("image/png".toMediaTypeOrNull())
                                )
                                .build()

                val request =
                        Request.Builder()
                                .url(
                                        "https://stag-cypress-api.klaytnfinder.io/papi/v1/contracts/$contractAddress"
                                )
                                .put(requestBody)
                                .addHeader("accept", "application/json")
                                .addHeader("content-type", "multipart/form-data")
                                .build()

                println("[${index.incrementAndGet()}] $contractAddress[$symbol] => $file")
                if (!dryRun) {
                    val response = client.newCall(request).execute()
                    println(response.code)
                }
            }
        }
    }
}
