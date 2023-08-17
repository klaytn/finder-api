package io.klaytn.finder.compiler.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.compiler.interfaces.rest.License
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class SolidityCompiler(
    private val solidityBuildFileManager: SolidityBuildFileManager,
) {
    private val logger = logger(javaClass)

    fun compile(file: File, option: Option): List<CompileResult> {
        val compiler = solidityBuildFileManager.getCompiler(option.version)

        val command = arrayOf(
            compiler.value.absolutePath,
            option.toCommand(compiler.key.version),
            file.absolutePath
        ).joinToString(" ")

        logger.info("[start] command: $command")
        val output = command.execute() ?: return emptyList()
        logger.info("[  end] output: ${output.toPrettyString()}")

        return parse(output)
    }

    private fun parse(json: JsonNode): List<CompileResult> {
        val result = mutableListOf<CompileResult>()
        val tree = json.at("/contracts")

        tree.fields().forEach { entry ->
            val name = entry.key.substringAfter(":")
            val node = entry.value

            val abi = node.at("/abi")
            val bin = node.at("/bin").asText()
            val binRuntime = node.at("/bin-runtime").asText()
            val hashes = mutableMapOf<String, String>()

            node.at("/hashes").fields().forEach { hash ->
                hashes[hash.key] = hash.value.asText()
            }

            val parsed = parseABI(abi)
            result.add(
                CompileResult(
                    name = name,
                    abi = abi.toString(),
                    binary = bin,
                    runtimeBinary = binRuntime,
                    hashes = hashes,
                    abiFunctions = parsed.first,
                    abiEvents = parsed.second
                )
            )
        }

        return result
    }

    private fun parseABI(abi: JsonNode): Pair<List<ABIFunction>, List<ABIEvent>> {
        val functions = mutableListOf<ABIFunction>()
        val events = mutableListOf<ABIEvent>()

        abi.forEach { node ->
            when (node.at("/type").asText()) {
                "function" -> functions.add(parseABIFunction(node))
                "event" -> events.add(parseABIEvent(node))
            }
        }

        return Pair(functions, events)
    }

    private fun parseABIFunction(tree: JsonNode) = ABIFunction(
        constant = tree.at("/constant").asBoolean(),
        inputs = parseABIFunctionParams(tree.at("/inputs")),
        name = tree.at("/name").asText(),
        outputs = parseABIFunctionParams(tree.at("/outputs")),
        payable = tree.at("/payable").asBoolean(),
        stateMutability = tree.at("/stateMutability").asText()
    )

    private fun parseABIFunctionParams(tree: JsonNode) = tree.map { node ->
        ABIFunctionParam(node.at("/name").asText(), node.at("/type").asText())
    }

    private fun parseABIEvent(tree: JsonNode) = ABIEvent(
        anonymous = tree.at("/anonymous").asBoolean(),
        inputs = parseABIEventParams(tree.at("/inputs")),
        name = tree.at("/name").asText()
    )

    private fun parseABIEventParams(tree: JsonNode) = tree.map { node ->
        ABIEventParam(node.at("/indexed").asBoolean(), node.at("/name").asText(), node.at("/type").asText())
    }

    data class Version(
        val major: Int,
        val minor: Int,
        val patch: Int,
    ) {
        constructor(version: String) : this(
            version.split(".")[0].toInt(),
            version.split(".")[1].toInt(),
            version.split(".")[2].toInt()
        )

        fun isGreaterOrEqualsTo(version: Version): Boolean {
            val lowerVersion = Version(version.major, version.minor, version.patch - 1)

            if (major > lowerVersion.major) {
                return true
            }

            if (major < lowerVersion.major) {
                return false
            }

            if (minor > lowerVersion.minor) {
                return true
            }

            if (minor < lowerVersion.minor) {
                return false
            }

            return patch > lowerVersion.patch
        }
    }

    data class Option(
        val version: String,
        val license: String = License.None.text,
        val optimize: Boolean,
        val optimizeRuns: Long = 200,
        val evmVersion: String? = null,
        val libraries: Map<String, String>? = null,
    ) {
        // since 0.4.21: supports --evm-version
        // since 0.8.1: libraries
        fun toCommand(compilerVersion: String) = arrayOf(
            if (optimize) "--optimize --optimize-runs $optimizeRuns" else null,
            if (supports(compilerVersion, "0.4.21")) evmVersion?.let { "--evm-version $it" } else null,
            libraries?.let { library ->
                "--libraries " + library.entries.joinToString(",") {
                    it.key + (if (supports(compilerVersion, "0.8.1")) "=" else ":") + it.value
                }
            },
            "--combined-json abi,bin,bin-runtime,hashes"
        ).filterNotNull().joinToString(" ")

        private fun supports(compilerVersionStr: String, supportedVersionStr: String): Boolean {
            val compilerVersion = Version(compilerVersionStr)
            val supportedVersion = Version(supportedVersionStr)

            return compilerVersion.isGreaterOrEqualsTo(supportedVersion)
        }
    }

    private fun String.execute(
        workingDir: File = File("."),
        timeout: Long = 30,
        timeoutUnit: TimeUnit = TimeUnit.SECONDS,
    ) =
        runCatching {
            val tempFile = File("/tmp/scope-${UUID.randomUUID()}.json")
            try {
                ProcessBuilder("\\s".toRegex().split(this))
                    .directory(workingDir)
                    .redirectOutput(tempFile)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start().also {
                        it.waitFor(timeout, timeoutUnit)
                    }
                jacksonObjectMapper().readTree(tempFile.inputStream())
            } finally {
                tempFile.delete()
            }
        }.onFailure {
            logger.error(it.message, it)
        }.getOrNull()
}

data class CompileResult(
    val name: String,
    val abi: String,
    val binary: String,
    val runtimeBinary: String,
    val hashes: Map<String, String>,
    val abiFunctions: List<ABIFunction>,
    val abiEvents: List<ABIEvent>,
)

data class ABIFunction(
    val constant: Boolean,
    val inputs: List<ABIFunctionParam>,
    val name: String,
    val outputs: List<ABIFunctionParam>,
    val payable: Boolean,
    val stateMutability: String,
)

data class ABIEvent(
    val anonymous: Boolean,
    val inputs: List<ABIEventParam>,
    val name: String,
)

data class ABIFunctionParam(
    val name: String,
    val type: String,
)

data class ABIEventParam(
    val indexed: Boolean,
    val name: String,
    val type: String,
)
