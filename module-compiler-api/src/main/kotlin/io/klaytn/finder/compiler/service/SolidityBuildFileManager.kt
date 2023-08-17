package io.klaytn.finder.compiler.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.File

class SolidityBuildFileManager {
    private val solidity = mutableMapOf<Build, File>()

    fun versions() = solidity.keys.map { it.longVersion }

    fun set(build: Build, file: File) {
        solidity[build] = file
    }

    fun contains(build: Build) = solidity.containsKey(build)

    fun getCompiler(version: String) =
            solidity.entries.find { it.key.longVersion == version }
                    ?: throw IllegalArgumentException("cannot find version: ${version}")
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Build(
        val path: String,
        val version: String,
        val build: String,
        val longVersion: String,
)
