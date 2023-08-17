package io.klaytn.finder.view.mapper.transaction.status

import io.klaytn.commons.model.mapper.Mapper
import io.klaytn.finder.domain.mysql.set1.Transaction
import io.klaytn.finder.view.model.transaction.TransactionErrorType
import io.klaytn.finder.view.model.transaction.TransactionStatus
import io.klaytn.finder.view.model.transaction.TransactionStatusView
import org.springframework.stereotype.Component

@Component
class TransactionToStatusViewMapper : Mapper<Transaction, TransactionStatusView> {
    override fun transform(source: Transaction): TransactionStatusView {
        return TransactionStatusView(
            status = if (source.status == 1) TransactionStatus.Success else TransactionStatus.Fail,
            failMessage = source.txError?.let {
                val error = TransactionErrorType.of(it)
                error.desc + " - uint($it)"
            }
        )
    }
}