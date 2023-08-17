package io.klaytn.finder.service

import io.klaytn.finder.config.dynamic.FinderServerPaging
import io.klaytn.finder.domain.mysql.set3.EventLog
import io.klaytn.finder.domain.mysql.set3.EventLogRepository
import io.klaytn.finder.infra.cache.CacheName
import io.klaytn.finder.infra.cache.CacheUtils
import io.klaytn.finder.infra.utils.PageUtils
import io.klaytn.finder.infra.web.model.SimplePageRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class EventLogService(
    private val eventLogCachedService: EventLogCachedService,
) {
    fun getEventLogsByTransactionHash(
        transactionHash: String, signature: String?, simplePageRequest: SimplePageRequest
    ): Page<EventLog> {
        val page = eventLogCachedService.getEventLogIdsByTransactionHash(transactionHash, signature, simplePageRequest)
        val contents = page.content.map { it.id }.run { eventLogCachedService.getEventLogs(this) }

        return PageUtils.getPage(contents, simplePageRequest, page.totalElements)
    }

    fun getEventLogsByAccountAddress(
        accountAddress: String,
        signature: String?,
        blockNumberRange: LongRange? = null,
        simplePageRequest: SimplePageRequest
    ): Page<EventLog> {
        val count = eventLogCachedService.countEventLogsByAccountAddress(accountAddress, signature, blockNumberRange)
        PageUtils.checkPageParameter(simplePageRequest, count)

        val page = eventLogCachedService.getEventLogIdsByAccountAddress(
            accountAddress, signature, blockNumberRange, simplePageRequest)
        val contents = page.map { it.id }.run { eventLogCachedService.getEventLogs(this) }
        return PageUtils.getPage(contents, simplePageRequest, count)
    }
}

@Service
class EventLogCachedService(
    private val eventLogRepository: EventLogRepository,
    private val cacheUtils: CacheUtils,
    private val finderServerPaging: FinderServerPaging
) {
    val sortForTransactionHash = Sort.by(
        Sort.Order.desc("logIndex"))

    val sortForAddress = Sort.by(
        Sort.Order.desc("blockNumber"),
        Sort.Order.desc("transactionIndex"),
        Sort.Order.desc("logIndex"))

    fun getEventLogIdsByTransactionHash(
        transactionHash: String, signature: String?, simplePageRequest: SimplePageRequest
    ) =
        with(simplePageRequest.pageRequest(sortForTransactionHash)) {
            if(signature.isNullOrBlank()) {
                eventLogRepository.findAllByTransactionHash(transactionHash, this)
            } else {
                eventLogRepository.findAllByTransactionHashAndSignature(transactionHash, signature, this)
            }
        }

    fun getEventLogIdsByAccountAddress(
        accountAddress: String,
        signature: String?,
        blockNumberRange: LongRange? = null,
        simplePageRequest: SimplePageRequest
    ) =
        with(simplePageRequest.pageRequest(sortForAddress)) {
            if(blockNumberRange == null) {
                if(signature.isNullOrBlank()) {
                    eventLogRepository.findAllByAddress(accountAddress, this)
                } else {
                    eventLogRepository.findAllByAddressAndSignature(accountAddress, signature,this)
                }

            } else {
                if(signature.isNullOrBlank()) {
                    eventLogRepository.findAllByAddressAndBlockNumberBetween(
                        accountAddress, blockNumberRange.first, blockNumberRange.last, this)
                } else {
                    eventLogRepository.findAllByAddressAndSignatureAndBlockNumberBetween(
                        accountAddress, signature, blockNumberRange.first, blockNumberRange.last, this)
                }
            }
        }

    fun countEventLogsByAccountAddress(
        accountAddress: String,
        signature: String?,
        blockNumberRange: LongRange? = null,
    ) =
        with(finderServerPaging.limit.eventLog) {
            if(blockNumberRange == null) {
                if(signature.isNullOrBlank()) {
                    eventLogRepository.countAllByAddress(accountAddress, this)
                } else {
                    eventLogRepository.countAllByAddressAndSignature(accountAddress, signature, this)
                }
            } else {
                if(signature.isNullOrBlank()) {
                    eventLogRepository.countAllByAddressAndBlockNumberBetween(
                        accountAddress, blockNumberRange.first, blockNumberRange.last, this)
                } else {
                    eventLogRepository.countAllByAddressAndSignatureAndBlockNumberBetween(
                        accountAddress, signature, blockNumberRange.first, blockNumberRange.last, this)
                }
            }
        }

    fun getEventLogs(searchIds: List<Long>) =
        cacheUtils.getEntities(CacheName.EVENT_LOG, EventLog::class.java, searchIds, eventLogRepository)
}
