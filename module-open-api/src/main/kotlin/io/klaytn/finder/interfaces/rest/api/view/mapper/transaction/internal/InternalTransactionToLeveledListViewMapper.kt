package io.klaytn.finder.interfaces.rest.api.view.mapper.transaction.internal

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.klaytn.commons.model.mapper.ListMapper
import io.klaytn.finder.domain.mysql.set2.InternalTransaction
import io.klaytn.finder.interfaces.rest.api.view.mapper.transaction.input.TransactionToInputDataViewMapper
import io.klaytn.finder.interfaces.rest.api.view.model.transaction.internal.InternalTransactionLeveledListView
import org.springframework.stereotype.Component

@Component
class InternalTransactionToLeveledListViewMapper(
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
                from = it.from.address,
                to = it.to?.address,
                amount = it.value,
                gasLimit = it.gas,
                inputData = inputDataViewMapper.transformSingle(it.input),
                outputData = it.output,
                error = it.error,
                reverted = it.reverted?.let { reverted -> jacksonObjectMapper().readValue(reverted) }
            )
        }
    }
}
