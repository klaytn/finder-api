package io.klaytn.finder.service

import io.klaytn.commons.model.env.Phase
import io.klaytn.finder.config.ChainBlockMintProperty
import io.klaytn.finder.domain.mysql.set1.GasPrice
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.infra.utils.KlayUtils
import io.klaytn.finder.service.caver.TestCaverChainType
import io.klaytn.finder.service.db.TestDbConstant
import okhttp3.internal.closeQuietly
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileInputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class BlockProposedCreator {
    private val defaultBlockMintValue = BigDecimal(9.6)
    private val yearMonth = "202212"

    @Test
    fun dumpBlocks() {
        val startDate =
            LocalDateTime.of(yearMonth.substring(0, 4).toInt(), yearMonth.substring(4, 6).toInt(), 1, 0, 0, 0, 0)
        val endDate = startDate.plusMonths(1).minusSeconds(1).withNano(0)

        val timestampRange = LongRange(
            DateUtils.localDateTimeToEpochMilli(startDate) / 1000,
            DateUtils.localDateTimeToEpochMilli(endDate) / 1000
        )

        val hikariDataSource = TestDbConstant.getDatasource(
            Phase.prod, TestCaverChainType.CYPRESS, TestDbConstant.TestDbType.SET0101, true
        )

        hikariDataSource.use { dataSource ->
            dataSource.connection.use { connection ->
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
                            's3://GCS_PRIVATE_BUCKET/finder/cypress/proposed-blocks/source/temp_${yearMonth}.cvs'
                        FORMAT CSV OVERWRITE ON
                    """.trimIndent()
                        ).execute()
                    }
                }
            }
        }
    }

    @Test
    fun createBlockProposer() {
        val rootPath = "/Users/marcusmoon/projects/finder-static/proposed_block/cypress"

        val dateformat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        val dynamicFeeStartBlockNumber = 99841497
        val gasPrices = listOf(
            GasPrice(1, 87091199, BigDecimal("0.000000025")),
            GasPrice(87091200, 91324799, BigDecimal("0.00000075")),
            GasPrice(91324800, dynamicFeeStartBlockNumber - 1L, BigDecimal("0.00000025"))
        )

        val blockMintProperties = listOf(
            ChainBlockMintProperty(0, defaultBlockMintValue),
            ChainBlockMintProperty(106444800, BigDecimal(6.4)),
        )

        val blockSources = listOf(
            "temp_${yearMonth}.cvs.txt",
        )

        val fileCheckMap = mutableMapOf<File, Boolean>()
        blockSources.forEach { blockSource ->
            println("[$blockSource] started....")

            val counter = AtomicInteger(0)
            val file = File("$rootPath/source/$blockSource")
            val scanner = Scanner(FileInputStream(file), StandardCharsets.UTF_8)
            try {
                while (scanner.hasNextLine()) {
                    // source : proposer, number, timestamp, transaction_count, gas_used, size
                    // dest   : "blockId", "datetime", "totalTransactionCount", "rewardKlay", "blockSize"
                    val source = scanner.nextLine().split(",")

                    val proposer = source[0].replace("\"", "")
                    val number = source[1].toLong()
                    val timestamp = source[2].toLong()
                    val transactionCount = source[3].toInt()
                    val gasUsed = source[4].toInt()
                    val size = source[5].toLong()
                    val baseFeePerGas = KlayUtils.pebToKlay(source[6].replace("\"", ""))

                    if (counter.incrementAndGet() % 100000 == 0) {
                        println("[$blockSource] ${counter.get()}")
                    }

                    // 생성할 date 추출
                    val localDateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(timestamp * 1000),
                        TimeZone.getDefault().toZoneId()
                    )
                    val date = localDateTime.format(DateTimeFormatter.ofPattern("yyyyMM"))

                    // reward
                    val gasPrice =
                        if (number >= dynamicFeeStartBlockNumber)
                            baseFeePerGas
                        else
                            gasPrices.firstOrNull { number in it.minBlockNumber..it.maxBlockNumber }?.gasPrice
                                ?: BigDecimal.ZERO
                    var fees = gasUsed.toBigDecimal() * gasPrice
                    if (number >= dynamicFeeStartBlockNumber) {
                        fees = fees.divide(BigDecimal(2))
                    }
                    val mintedValue = blockMintProperties
                        .sortedByDescending { it.startBlockNumber }
                        .firstOrNull() { number >= it.startBlockNumber }?.mintValue ?: defaultBlockMintValue
                    val rewardKlay = (mintedValue + fees).setScale(10, RoundingMode.HALF_UP)

                    // to dest converter
                    val datetime = dateformat.format(DateUtils.from(timestamp.toInt()))
                    val dest = "$number,$datetime,$transactionCount,$rewardKlay,$size"

                    File("$rootPath/tmp-csv/${date}").mkdirs()
                    val csvFile = File("$rootPath/tmp-csv/${date}/proposed_blocks_${date}_${proposer}.csv")
                    if (fileCheckMap[csvFile] == null) {
                        fileCheckMap[csvFile] = true
                        csvFile.appendText("block,date,transactions,reward(klay),size(byte)\n", StandardCharsets.UTF_8)
                    }
                    csvFile.appendText(dest + "\n", StandardCharsets.UTF_8)
                }

                println("[$blockSource] end....")
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                scanner.closeQuietly()
            }
        }
    }
}