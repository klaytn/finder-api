package io.klaytn.finder.service

import java.io.File
import java.nio.file.Files
import java.util.*
import org.junit.jupiter.api.Test

class ImageFileTest {
    @Test
    fun test() {
        val file = File("src/test/resources/image/test.png")
        println(String(Base64.getEncoder().encode(file.readBytes())))
        println(Files.probeContentType(file.toPath()))
    }
}
