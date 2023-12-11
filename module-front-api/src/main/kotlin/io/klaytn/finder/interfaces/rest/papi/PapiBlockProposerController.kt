package io.klaytn.finder.interfaces.rest.papi

import com.zaxxer.hikari.HikariDataSource
import io.klaytn.commons.model.response.SimpleResponse
import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.config.ChainProperties
import io.klaytn.finder.config.FinderGcsProperties
import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.infra.utils.KlayUtils
import io.klaytn.finder.infra.web.swagger.SwaggerConstant
import io.klaytn.finder.service.BlockRewardDelegator
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.tags.Tag
import okhttp3.internal.closeQuietly
import org.apache.commons.io.FileUtils
import org.springframework.context.annotation.Profile
import org.springframework.util.FileCopyUtils
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import com.google.cloud.storage.Storage
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@Profile(ServerMode.PRIVATE_API_MODE)
@RestController
@Tag(name = SwaggerConstant.TAG_PRIVATE)
class PapiBlockProposerController(
    private val set1DataSource: HikariDataSource,
    private val gcsClient: Storage,
//    private val gcsAsyncClient: Storage,
    private val chainProperties: ChainProperties,
    private val finderGcsProperties: FinderGcsProperties,
    private val blockRewardDelegator: BlockRewardDelegator
) {
    private val logger = logger(this::class.java)

    @Operation(
        description = "Generate a list of blocks for a specific period to create block reward information.",
        parameters = [
            Parameter(name = "yearMonth", description = "Period of the block list to generate", `in` = ParameterIn.PATH),
        ]
    )
    @PostMapping("/papi/v1/block-proposers/{yearMonth}/source")
    fun createBlockProposerSource(
        @PathVariable yearMonth: String
    ): SimpleResponse<Boolean> {
        try {
            createBlockProposerSourceFiles(yearMonth)
        } catch (exception: Exception) {
            logger.warn(exception.message, exception)
            return SimpleResponse(false)
        }
        return SimpleResponse(true)
    }

    @Operation(
        description = "Generate a CSV containing block reward information for each GC using the block information within the specified period and upload it to S3.",
        parameters = [
            Parameter(name = "yearMonth", description = "Period of the block list to generate", `in` = ParameterIn.PATH),
        ]
    )
    @PostMapping("/papi/v1/block-proposers/{yearMonth}/csv")
    fun createBlockProposerCSV(
        @PathVariable yearMonth: String,
    ): SimpleResponse<Boolean> {
        try {
            val sourceFile = downloadSourceFile(yearMonth)
            checkAndUploadBlockProposerCSV(yearMonth, sourceFile)
        } catch (exception: Exception) {
            logger.warn(exception.message, exception)
            return SimpleResponse(false)
        }
        return SimpleResponse(true)
    }

    private fun createBlockProposerSourceFiles(yearMonth: String) {
        val startDate =
            LocalDateTime.of(yearMonth.substring(0, 4).toInt(), yearMonth.substring(4, 6).toInt(), 1, 0, 0, 0, 0)
        val endDate = startDate.plusMonths(1).minusSeconds(1).withNano(0)

        val timestampRange = LongRange(
            DateUtils.localDateTimeToEpochMilli(startDate) / 1000,
            DateUtils.localDateTimeToEpochMilli(endDate) / 1000
        )

        set1DataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                    SELECT
                        min(number) as startBlockNumber, max(number) as endBlockNumber
                    FROM
                        blocks
                    where
                        timestamp between ${timestampRange.first} and ${timestampRange.last}
                """.trimIndent()
            ).executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    val blockRange = LongRange(
                        resultSet.getLong("startBlockNumber"),
                        resultSet.getLong("endBlockNumber")
                    )
                    connection.prepareStatement(
                        """
                        SELECT
                            proposer, number, timestamp, transaction_count, gas_used, size, base_fee_per_gas
                        FROM
                            blocks
                        where
                            number between ${blockRange.first} and ${blockRange.last}
                        order by
                            number asc
                        INTO OUTFILE S3
                            's3://klaytn-prod-finder-private/finder/${chainProperties.type}/proposed-blocks/source/temp_${yearMonth}.csv'
                        FORMAT CSV OVERWRITE ON
                        """.trimIndent()
                    ).execute()

                    // todo new reward
//                    connection.prepareStatement(
//                        """
//                        SELECT
//                            b.proposer, b.number, b.timestamp, b.transaction_count, b.gas_used, b.size, b.base_fee_per_gas,
//                            br.minted, br.total_fee, br.burnt_fee
//                        FROM
//                            blocks b LEFT OUTER JOIN block_rewards as br on b.number = br.number
//                        where
//                            b.number between ${blockRange.first} and ${blockRange.last}
//                        order by
//                            b.number asc
//                        INTO OUTFILE S3
//                            's3://klaytn-prod-finder-private/finder/${chainProperties.type}/proposed-blocks/source/temp_${yearMonth}.csv'
//                        FORMAT CSV OVERWRITE ON
//                        """.trimIndent()
//                    ).execute()
                }
            }
        }
    }

    private fun checkAndUploadBlockProposerCSV(yearMonth: String, sourceFile: File?) {
        if (sourceFile == null) {
            logger.warn("$yearMonth not found.")
            return
        }

        val tempDir = System.getProperty("java.io.tmpdir")
        val dateformat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        val fileCheckMap = mutableMapOf<File, MutableList<String>>()

        val counter = AtomicInteger(0)
        val scanner = Scanner(FileInputStream(sourceFile), StandardCharsets.UTF_8)
        try {
            val tempRootPath = "$tempDir/tmp-csv/${yearMonth}"
            File(tempRootPath).mkdirs()

            while (scanner.hasNextLine()) {
                // source : proposer, number, timestamp, transaction_count, gas_used, size, minted, total_fee, burnt_fee
                // dest   : "blockId", "datetime", "totalTransactionCount", "rewardKlay", "blockSize"
                val source = scanner.nextLine().split(",")

                val proposer = source[0].replace("\"", "")
                val number = source[1].toLong()
                val timestamp = source[2].toLong()
                val transactionCount = source[3].toInt()
                val gasUsed = source[4].toInt()
                val size = source[5].toLong()

                // todo new reward
//                val minted =
//                    if(source[6].isNotBlank()) {
//                        source[6].substring(2).toBigInteger(16)
//                    } else null
//                val totalFee =
//                    if(source[7].isNotBlank()) {
//                        source[7].substring(2).toBigInteger(16)
//                    } else null
//                val burntFee =
//                    if(source[8].isNotBlank()) {
//                        source[8].substring(2).toBigInteger(16)
//                    } else null
//                val blockReward = minted?.let {
//                    BlockReward(
//                        number = number,
//                        minted = minted,
//                        totalFee = totalFee!!,
//                        burntFee = burntFee!!,
//                        proposer = BigInteger.ZERO,
//                        stakers = BigInteger.ZERO,
//                        kgf = BigInteger.ZERO,
//                        kir = BigInteger.ZERO,
//                        rewards = ""
//                    )
//                }

                val baseFeePerGas = KlayUtils.pebToKlay(source[6].replace("\"", ""))
                val datetime = dateformat.format(DateUtils.from(timestamp.toInt()))

                // todo new reward
                val rewardKlay = blockRewardDelegator.getRewardBelowMagnaVersion(number, baseFeePerGas, gasUsed)
//                val blockRewardItem = blockRewardDelegator.getRewardItem(number, baseFeePerGas, gasUsed, blockReward)
//                val rewardKlay = blockRewardItem.totalReward()

                if (counter.incrementAndGet() % 100000 == 0) {
                    writeBlockRewardToFile(sourceFile, counter, fileCheckMap)
                }

                val csvFile = File("$tempRootPath/proposed_blocks_${yearMonth}_${proposer}.csv")
                if (fileCheckMap[csvFile] == null) {
                    fileCheckMap[csvFile] = mutableListOf()
                    fileCheckMap[csvFile]!!.add("block,date,transactions,reward(klay),size(byte)")
                }
                fileCheckMap[csvFile]!!.add("$number,$datetime,$transactionCount,$rewardKlay,$size")
            }
            writeBlockRewardToFile(sourceFile, counter, fileCheckMap)

            // GCP
            val bucket = gcsClient.get(finderGcsProperties.privateBucket)
            val path = "finder/${chainProperties.type}/proposed-blocks/csv/$yearMonth/"
            val inputStream = Files.newInputStream(Path.of(tempRootPath))
            val blob = bucket.create(
                path,
                inputStream,
                "text/csv",
            )
            inputStream.close()
            logger.info("${blob.name} upload complete.")
        } catch (e: Exception) {
            logger.warn(e.message, e)
        } finally {
            logger.info("[$sourceFile] end....")
            scanner.closeQuietly()
            FileUtils.deleteQuietly(sourceFile)
            fileCheckMap.keys.forEach { FileUtils.deleteQuietly(it) }
        }
    }

    private fun downloadSourceFile(yearMonth: String): File? {
        try {
            val filename = "temp_${yearMonth}.csv.part_00000"
            val key = "finder/${chainProperties.type}/proposed-blocks/source/$filename"

            // gcp
            val getObjectResponse = gcsClient.get(finderGcsProperties.privateBucket).get(key)
            val tempFile = File.createTempFile("temp_", ".csv")
            val tempFileOutputStream = FileOutputStream(tempFile)
            FileCopyUtils.copy(getObjectResponse.getContent(), tempFileOutputStream)
            tempFileOutputStream.close()
            return tempFile
        } catch (_: Exception) {
            logger.warn("[$yearMonth] not found.")
        }
        return null
    }

    private fun writeBlockRewardToFile(
        sourceFile: File,
        counter: AtomicInteger,
        fileCheckMap: Map<File,MutableList<String>>
    ) {
        logger.info("[$sourceFile] ${counter.get()}")
        fileCheckMap.forEach { (file, data) ->
            val fileWriter = FileWriter(file, true)
            data.forEach{
                fileWriter.write(it + "\n")
            }
            fileWriter.close()
            data.clear()
        }
    }
}
