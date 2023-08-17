package io.klaytn.finder.interfaces.rest.api.view.mapper.transaction.status

import com.klaytn.caver.methods.response.TransactionReceipt.TransactionReceiptData
import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.view.model.transaction.TransactionErrorType
import io.klaytn.finder.view.model.transaction.TransactionStatus
import io.klaytn.finder.view.model.transaction.TransactionStatusView
import org.springframework.stereotype.Component

@Component
class TransactionReceiptToStatusViewMapper : Mapper<TransactionReceiptData, TransactionStatusView> {
    override fun transform(source: TransactionReceiptData): TransactionStatusView {
        return TransactionStatusView(
            status = if (Integer.decode(source.status) == 1) TransactionStatus.Success else TransactionStatus.Fail,
            failMessage = source.txError?.let {
                val error = TransactionErrorType.of(Integer.decode(it))
                error.desc + " - uint($it)"
            }
        )
    }
}