package io.klaytn.finder.service.caver

import com.fasterxml.jackson.annotation.JsonProperty
import com.klaytn.caver.Caver
import com.klaytn.caver.methods.response.TransactionReceipt.TransactionReceiptData
import io.klaytn.commons.utils.logback.logger
import org.springframework.stereotype.Service
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.Response

@Service
class CaverTransactionService(
    private val caver: Caver,
) {
    private val logger = logger(this::class.java)

    fun getTransactionReceipt(transactionHash: String): TransactionReceiptData? =
        try {
            val getTransactionReceipt = caver.rpc.klay.getTransactionReceipt(transactionHash).send()
            if(!getTransactionReceipt.hasError()) {
                getTransactionReceipt.result
            } else {
                null
            }
        } catch (exception: Exception) {
            logger.warn(exception.message, exception)
            null
        }

    fun getPendingStatus(): TransactionPoolResponse.TransactionPoolStatus? =
        try {
            val pendingStatusResult = Request<Any, TransactionPoolResponse>("txpool_status",
                emptyList(),
                caver.rpc.web3jService,
                TransactionPoolResponse::class.java).send()
            if(!pendingStatusResult.hasError()) {
                pendingStatusResult.result
            } else {
                null
            }
        } catch (exception: Exception) {
            logger.warn(exception.message, exception)
            null
        }
}

class TransactionPoolResponse : Response<TransactionPoolResponse.TransactionPoolStatus>() {
    data class TransactionPoolStatus(val pending: Int, val queued: Int) {
        constructor(@JsonProperty("pending") pending: String, @JsonProperty("queued") queued: String) :
                this(toInt(pending), toInt(queued))

        companion object {
            fun toInt(value: String) = value.substring(2).toInt(16)
        }
    }
}