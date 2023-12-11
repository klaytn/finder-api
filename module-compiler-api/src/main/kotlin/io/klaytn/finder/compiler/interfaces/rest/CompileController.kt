package io.klaytn.finder.compiler.interfaces.rest

import io.klaytn.finder.compiler.service.CompileResult
import io.klaytn.finder.compiler.service.SolidityBuildFileManager
import io.klaytn.finder.compiler.service.SolidityCompiler
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.swagger.v3.oas.annotations.tags.Tag
import org.apache.commons.io.IOUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.io.FileOutputStream
import kotlin.text.Charsets.UTF_8

@Suppress("UNUSED_EXPRESSION")
@RestController
@Tag(name= SwaggerConstant.TAG_PUBLIC)
class CompileController(
    private val solidityCompiler: SolidityCompiler,
    private val solidityBuildFileManager: SolidityBuildFileManager,
) {
    @GetMapping("/")
    fun healthCheck() = "OK"

    @GetMapping("/options")
    fun options(): OptionResult {
        val licenses = License.values().map { it.text }
        val solidityVersions = solidityBuildFileManager.versions().sortedWith { v1: String, v2: String ->
            val secondVersion = v1.substringBefore("+commit").split(".").map { it.toInt() }
            val firstVersion = v2.substringBefore("+commit").split(".").map { it.toInt() }

            val levelOne = firstVersion[0].compareTo(secondVersion[0])
            val levelTwo = firstVersion[1].compareTo(secondVersion[1])
            val levelThree = firstVersion[2].compareTo(secondVersion[2])

            if (levelOne != 0) levelOne
            else if (levelTwo != 0) levelTwo
            else if (levelThree != 0) levelThree
            else
                0
        }
        val evmVersions = EvmVersion.values().sortedDescending().map { EvmVersionMap(it.name, it.text) }
        return OptionResult(licenses, solidityVersions, evmVersions)
    }

    @PostMapping("/compile")
    fun compile(@RequestBody request: CompileRequest): List<CompileResult> {
        val solidity = File.createTempFile("scope-", ".sol", File("/tmp")).apply {
            FileOutputStream(this).use {
                IOUtils.write(request.solidity, it, UTF_8)
            }
        }

        return solidityCompiler.compile(
            solidity, SolidityCompiler.Option(
                version = request.version,
                license = request.license,
                optimize = request.optimize,
                optimizeRuns = request.optimizeRuns,
                evmVersion = request.evmVersion,
                libraries = request.libraries
            )
        ).also {
            solidity.delete()
        }
    }

    data class CompileRequest(
        val version: String,
        val license: String,
        val optimize: Boolean,
        val optimizeRuns: Long,
        val evmVersion: String?,
        val libraries: Map<String, String>?,
        val solidity: String,
    )
}

data class OptionResult(
    val licenses: List<String>,
    val solidityVersions: List<String>,
    val evmVersions: List<EvmVersionMap>,
)

enum class License(val text: String) {
    None("No License"),
    Unlicense("The Unlicense"),
    MIT("MIT License"),
    GNU_GPLv2("GNU General Public License v2.0"),
    GNU_GPLv3("GNU General Public License v3.0"),
    GNU_LGPLv2_1("GNU Lesser General Public License v2.1"),
    GNU_LGPLv3("GNU Lesser General Public License v3.0"),
    BSD_2_Clause("BSD 2-clause \"Simplified\" license"),
    BSD_3_Clause("BSD 3-clause \"New\" Or \"Revised\" license"),
    MPL_2_0("Mozilla Public License 2.0"),
    OSL_3_0("Open Software License 3.0"),
    Apache_2_0("Apache 2.0"),
    GNU_AGPLv3("GNU Affero General Public License"),
    BSL_1_1("Business Source License"),
    ;
}

enum class EvmVersion(val text: String) {
    homestead("homestead (oldest version)"),
    tangerineWhistle("tangerineWhistle"),
    spuriousDragon("spuriousDragon"),
    byzantium("byzantium"),
    constantinople("constantinople"),
    petersburg("petersburg"),
    istanbul("istanbul"),
    berlin("berlin"),
    london("london"),
    default("default (compiler defaults)"),
    ;
}

data class EvmVersionMap(
    val name: String,
    val desc: String,
)