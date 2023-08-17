package io.klaytn.finder.interfaces.rest.api.view.mapper

import com.klaytn.caver.abi.ABI
import com.klaytn.caver.contract.ContractIOType
import io.klaytn.commons.model.mapper.ListMapper
import io.klaytn.finder.config.dynamic.FinderServerFeatureConfig
import io.klaytn.finder.domain.common.AccountAddress
import io.klaytn.finder.domain.common.KipType
import io.klaytn.finder.domain.mysql.set3.EventLog
import io.klaytn.finder.infra.utils.SignatureDecodeUtils
import io.klaytn.finder.interfaces.rest.api.view.model.EventLogItem
import io.klaytn.finder.interfaces.rest.api.view.model.EventLogListView
import io.klaytn.finder.view.model.account.AccountAddressView
import io.klaytn.finder.service.AccountAddressService
import io.klaytn.finder.service.SignatureService
import io.klaytn.finder.view.mapper.AccountAddressToViewMapper
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

@Component
class EventLogToListViewMapper(
    private val accountAddressService: AccountAddressService,
    private val signatureService: SignatureService,
    private val accountAddressToViewMapper: AccountAddressToViewMapper,
    private val finderServerFeatureConfig: FinderServerFeatureConfig
) : ListMapper<EventLog, EventLogListView> {
    override fun transform(source: List<EventLog>): List<EventLogListView> {
        val contractAccountAddressViewMap = source
            .associate { it.address to AccountAddress(it.address) }
            .also { accountAddressService.fillAccountAddress(it.values.toList()) }
            .map { it.key to accountAddressToViewMapper.transform(it.value) }.toMap()

        return source.map {
            getEventLogListView(it, contractAccountAddressViewMap[it.address])
        }
    }

    private fun getEventLogListView(source: EventLog, contractAccountAddressView: AccountAddressView?): EventLogListView {
        val topics = source.topics
        if(topics.isEmpty()) {
            return unknownEvent(source, contractAccountAddressView)
        }

        var estimatedEventLog = false
        var searchedEventLogInfo = getPredefinedEventLogItems(topics, source.data)
        if(searchedEventLogInfo == null && finderServerFeatureConfig.estimatedEventLog) {
            val eventSignatures = signatureService.getEventSignatures(topics[0])
            searchedEventLogInfo = eventSignatures.firstNotNullOfOrNull {
                runCatching {
                    getEstimatedEventLogItems(it.textSignature, topics, source.data)
                }.getOrNull()
            }
            if(searchedEventLogInfo != null) {
                estimatedEventLog = true
            }
        }
        if(searchedEventLogInfo == null) {
            return unknownEvent(source, contractAccountAddressView)
        }

        return EventLogListView(
            logIndex = source.logIndex,
            contractAddress = source.address,
            contractAccount = contractAccountAddressView,
            type = searchedEventLogInfo.first,
            topics = source.topics,
            data = source.data,
            items = searchedEventLogInfo.second,
            blockNumber = source.blockNumber,
            transactionHash = source.transactionHash,
            estimatedEventLog = estimatedEventLog
        )
    }

    private fun unknownEvent(source: EventLog, contractAccountAddressView: AccountAddressView?) = EventLogListView(
        logIndex = source.logIndex,
        contractAddress = source.address,
        contractAccount = contractAccountAddressView,
        type = "Unknown",
        topics = source.topics,
        data = source.data,
        items = generateItems(source.topics, source.data),
        blockNumber = source.blockNumber,
        transactionHash = source.transactionHash,
        estimatedEventLog = false
    )

    private fun getPredefinedEventLogItems(topics: List<String>, data: String): Pair<String,List<EventLogItem>>? {
        val events = KipType.Events.get()
        val event = events[topics[0]]?.find { it.indexedParams.size == topics.size - 1 } ?: return null

        val contractIOTypes = mutableListOf<ContractIOType>()
        event.indexedParams.forEach { (name, type) ->
            contractIOTypes.add(ContractIOType(name, type, true))
        }
        event.nonIndexedParams.forEach { (name, type) ->
            contractIOTypes.add(ContractIOType(name, type, false))
        }
        return getEventLogItems(event.name, topics, data, contractIOTypes)
    }

    private fun getEstimatedEventLogItems(eventSignature: String, topics: List<String>, data: String): Pair<String, List<EventLogItem>> {
        val signatureTypes = SignatureDecodeUtils.getTypeOfSignature(eventSignature)

        val eventName = eventSignature.substringBefore("(")
        val contractIOTypes = signatureTypes.mapIndexed { index, type ->
            val indexed =
                if(topics.size > 1) {
                    index <= topics.size - 2
                } else {
                    false
                }
            ContractIOType(type, type, indexed)
        }.toList()
        return getEventLogItems(eventName, topics, data, contractIOTypes)
    }

    private fun getEventLogItems(
        name: String,
        topics: List<String>,
        data: String,
        contractIOTypes: List<ContractIOType>
    ): Pair<String, List<EventLogItem>> {
        val eventLogItems = mutableListOf<EventLogItem>()
        val decoded = ABI.decodeLog(contractIOTypes, data, topics)

        val index = AtomicInteger(0)
        decoded.indexedValues.forEach {
            val parameterName = contractIOTypes[index.getAndIncrement()].name
            eventLogItems.add(EventLogItem(parameterName, SignatureDecodeUtils.value(it.value)))
        }
        decoded.nonIndexedValues.forEach {
            val parameterName = contractIOTypes[index.getAndIncrement()].name
            eventLogItems.add(EventLogItem(parameterName, SignatureDecodeUtils.value(it.value)))
        }
        return Pair(name, eventLogItems)
    }

    private fun generateItems(topics: List<String>, data: String): List<EventLogItem> {
        val items = mutableListOf<EventLogItem>()

        if (topics.size == 1) {
            items.add(EventLogItem("Topic", topics[0]))
        } else {
            topics.forEachIndexed { index, topic ->
                items.add(EventLogItem("Topic $index", topic))
            }
        }
        items.add(EventLogItem("Data", data))
        return items
    }
}