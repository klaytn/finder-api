package io.klaytn.finder.interfaces.rest.api.view.mapper.transaction

import io.klaytn.commons.model.mapper.ListMapper
import io.klaytn.finder.domain.mysql.set1.Transaction
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.interfaces.rest.api.view.mapper.transaction.input.TransactionToInputDataViewMapper
import io.klaytn.finder.interfaces.rest.api.view.model.transaction.TransactionListView
import io.klaytn.finder.service.TransactionService
import io.klaytn.finder.view.mapper.transaction.status.TransactionToStatusViewMapper
import io.klaytn.finder.view.model.transaction.TransactionTypeView
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class TransactionToListViewMapper(
    val transactionService: TransactionService,
    private val transactionToInputDataViewMapper: TransactionToInputDataViewMapper,
    private val transactionToStatusViewMapper: TransactionToStatusViewMapper,
) : ListMapper<Transaction, TransactionListView> {
    override fun transform(source: List<Transaction>): List<TransactionListView> {
        val signaturesMap = transactionToInputDataViewMapper.transform(source).associateBy { it.originalValue }

        return source.map { transaction ->
            val transactionFee = transactionService.getTransactionFees(transaction)
            val burntFees = transactionService.getTransactionBurntFees(transaction)

            val inputDataView = signaturesMap[transaction.input]
            val signature = inputDataView?.decodedValue?.signature
            val methodId = transaction.getMethodId()

            TransactionListView(
                transactionHash = transaction.transactionHash,
                blockId = transaction.blockNumber,
                datetime = DateUtils.from(transaction.timestamp),
                from = transaction.from.address,
                to = transaction.to?.address,
                transactionType = TransactionTypeView.getView(transaction.type),
                amount = transaction.value,
                transactionFee = transactionFee,
                status = transactionToStatusViewMapper.transform(transaction),
                methodId = methodId,
                signature = signature,
                effectiveGasPrice = transaction.effectiveGasPrice ?: BigDecimal.ZERO,
                burntFees = burntFees
            )
        }
    }
}