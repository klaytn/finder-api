package io.klaytn.finder.interfaces.rest.api.view.mapper

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.klaytn.commons.model.mapper.ListMapper
import io.klaytn.finder.domain.mysql.set2.InternalTransaction
import io.klaytn.finder.infra.utils.DateUtils
import io.klaytn.finder.interfaces.rest.api.view.model.transaction.internal.InternalTransactionLeveledListView
import io.klaytn.finder.interfaces.rest.api.view.model.transaction.internal.InternalTransactionListView
import io.klaytn.finder.service.TransactionService
import io.klaytn.finder.view.mapper.AccountAddressToViewMapper
import io.klaytn.finder.view.mapper.transaction.status.TransactionToStatusViewMapper
import org.springframework.stereotype.Component

@Component
class InternalTransactionToListViewMapper(
    val accountAddressToViewMapper: AccountAddressToViewMapper,
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
                from = accountAddressToViewMapper.transform(it.from)!!,
                to = accountAddressToViewMapper.transform(it.to),
                amount = it.value,
                error = it.error,
                methodId = methodId,
                signature = signature,
                transactionStatus = transaction?.let { transactionToStatusViewMapper.transform(transaction) }
            )
        }
    }
}

@Component
class InternalTransactionToLeveledListViewMapper(
    val accountAddressToViewMapper: AccountAddressToViewMapper,
    private val inputDataViewMapper: TransactionToInputDataViewMapper,
) : ListMapper<InternalTransaction, InternalTransactionLeveledListView> {
    override fun transform(source: List<InternalTransaction>): List<InternalTransactionLeveledListView> {
        val levels = mutableMapOf<Int, Int>()

        source.forEach {
            if (it.parentCallId == null) {
                levels[it.callId] = 0
            } else {
                levels[it.callId] = (levels[it.parentCallId] ?: 0) + 1
            }
        }

        return source.map {
            InternalTransactionLeveledListView(
                callId = it.callId,
                parentCallId = it.parentCallId,
                level = levels[it.callId]!!,
                type = it.type,
                from = accountAddressToViewMapper.transform(it.from)!!,
                to = accountAddressToViewMapper.transform(it.to),
                amount = it.value,
                gasLimit = it.gas,
                inputData = inputDataViewMapper.transform(it.input),
                outputData = it.output,
                error = it.error,
                reverted = it.reverted?.let { reverted -> jacksonObjectMapper().readValue(reverted) }
            )
        }
    }
}
