package io.klaytn.finder.compiler.service

import java.io.File
import java.util.*

interface SolidityDownloader {
    fun getList(os: Os): List<Build>
    fun download(os: Os, build: Build): File?

    enum class Os(val path: String) {
        WINDOWS("windows-amd64"),
        LINUX("linux-amd64"),
        MACOS("macosx-amd64"),
        ;

        companion object {
            fun check(): Os? {
                val os = System.getProperty("os.name").lowercase(Locale.getDefault())

                return when {
                    os.contains("win") -> WINDOWS
                    os.contains("nix") || os.contains("nux") || os.contains("aix") -> LINUX
                    os.contains("mac") -> MACOS
                    else -> null
                }
            }
        }
    }
}
