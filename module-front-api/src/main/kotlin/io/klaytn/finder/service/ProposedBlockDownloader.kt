package io.klaytn.finder.service

import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.config.ChainProperties
import io.klaytn.finder.config.FinderS3Properties
import io.klaytn.finder.domain.mysql.set1.BlockReward
import io.klaytn.finder.domain.mysql.set1.ProposerBlock
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.infra.utils.KlayUtils
import org.springframework.stereotype.Service
import org.springframework.util.FileCopyUtils
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat

@Service
class ProposedBlockDownloader(
    private val s3Client: S3Client,
    private val blockService: BlockService,
    private val blockRewardDelegator: BlockRewardDelegator,
    private val chainProperties: ChainProperties,
    private val finderS3Properties: FinderS3Properties,
) {
    private val logger = logger(this::class.java)
    private val dateformat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

    fun downloadFrom(accountAddress: String, date: String, outputStream: OutputStream) {
        if(chainProperties.type.equals("baobab", ignoreCase = true)) {
            return
        }

        if(!downloadFromS3(accountAddress, date, outputStream)) {
            downloadFromDB(accountAddress, date, outputStream)
        }
    }

    /**
     * s3://AWS_S3_PRIVATE_BUCKET/finder/cypress/proposed-blocks/csv/
     */
    private fun downloadFromS3(accountAddress: String, date: String, outputStream: OutputStream): Boolean {
        try {
            val filename = "proposed_blocks_${date}_$accountAddress.csv"
            val key = "finder/${chainProperties.type}/proposed-blocks/csv/$date/$filename"
            val getObjectRequest = GetObjectRequest.builder().bucket(finderS3Properties.privateBucket).key(key).build()
            val getObjectResponse = s3Client.getObject(getObjectRequest)

            FileCopyUtils.copy(getObjectResponse, outputStream)
            return true
        } catch (noSuchKeyException: NoSuchKeyException) {
            logger.debug(noSuchKeyException.message, noSuchKeyException)
        }
        return false
    }

    private fun downloadFromDB(accountAddress: String, date: String, outputStream: OutputStream) {
        val outputStreamWriter = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
        outputStreamWriter.write("block,date,transactions,reward(klay),size(byte)\n")

        val fetchSize = 5000
        var currentBlockNumber = Long.MAX_VALUE
        val filterDate = "${date.substring(0,4)}-${date.substring(4,6)}"
        while (true) {
            val proposerBlocks = blockService.getBlocksByProposerAndDateAndNumber(
                ProposerBlock::class.java, accountAddress, date, currentBlockNumber, fetchSize
            )

            val blockRewardMap = mutableMapOf<Long, BlockReward>()
            proposerBlocks.map { it.number }.chunked(500).map {
                blockRewardMap.putAll(blockRewardDelegator.getBlockReward(it))
            }

            proposerBlocks.forEach {
                val datetime = dateformat.format(DateUtils.from(it.timestamp))
                if(datetime.startsWith(filterDate)) {
                    val baseFeePerGas = KlayUtils.pebToKlay(it.baseFeePerGas)
                    val blockRewardItem = blockRewardDelegator.getRewardItem(
                        it.number, baseFeePerGas, it.gasUsed, blockRewardMap[it.number])

                    val rewardKlay = blockRewardItem.totalReward()
                    val blockInfo = "${it.number},$datetime,${it.transactionCount},$rewardKlay,${it.size}\n"
                    outputStreamWriter.write(blockInfo)
                }
            }
            outputStreamWriter.flush()

            if (proposerBlocks.size < fetchSize) {
                break
            }
            currentBlockNumber = proposerBlocks.last().number
        }
    }
}
