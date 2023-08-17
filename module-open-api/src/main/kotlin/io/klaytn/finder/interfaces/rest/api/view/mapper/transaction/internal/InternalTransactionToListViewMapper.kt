package io.klaytn.finder.interfaces.rest.api.view.mapper.transaction.internal

import io.klaytn.commons.model.mapper.ListMapper
import io.klaytn.finder.domain.mysql.set2.InternalTransaction
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.interfaces.rest.api.view.mapper.transaction.input.TransactionToInputDataViewMapper
import io.klaytn.finder.interfaces.rest.api.view.model.transaction.internal.InternalTransactionListView
import io.klaytn.finder.service.TransactionService
import io.klaytn.finder.view.mapper.transaction.status.TransactionToStatusViewMapper
import org.springframework.stereotype.Component

@Component
class InternalTransactionToListViewMapper(
    private val transactionService: TransactionService,
    private val inputDataViewMapper: TransactionToInputDataViewMapper,
    private val transactionToStatusViewMapper: TransactionToStatusViewMapper,
) : ListMapper<InternalTransaction, InternalTransactionListView> {
    override fun transform(source: List<InternalTransaction>): List<InternalTransactionListView> {
        val blockNumberAndTransactionIndices = source.map { Pair(it.blockNumber, it.transactionIndex) }
            .groupBy { it.first }.entries.associate {
                it.key to it.value.map { it.second }
            }

        val transactions = blockNumberAndTransactionIndices.map {
            transactionService.getTransactionByBlockNumberAndTransactionIndices(it.key, it.value)
        }.flatten().groupBy { it.blockNumber }.entries.associate { entry ->
            entry.key to entry.value.associateBy { it.transactionIndex }
        }

        return source.map {
            val transaction = transactions[it.blockNumber]?.get(it.transactionIndex)

            val inputDataView = inputDataViewMapper.transform(it.input)
            val signature = inputDataView.decodedValue?.signature
            val methodId = it.getMethodId()

            InternalTransactionListView(
                callId = it.callId,
                blockId = it.blockNumber,
                transactionHash = transaction?.transactionHash,
                datetime = transaction?.timestamp?.let { timestamp -> DateUtils.from(timestamp) },
                type = it.type,
                from = it.from.address,
                to = it.to?.address,
                amount = it.value,
                error = it.error,
                methodId = methodId,
                signature = signature,
                transactionStatus = transaction?.let { transactionToStatusViewMapper.transform(transaction) }
            )
        }
    }
}
