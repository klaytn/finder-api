package io.klaytn.finder.service.signature

import io.klaytn.finder.domain.mysql.set1.signature.EventSignature
import io.klaytn.finder.domain.mysql.set1.signature.EventSignatureRepository
import io.klaytn.finder.infra.cache.CacheName
import io.klaytn.finder.infra.db.DbConstants
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EventSignatureService(
    private val eventSignatureRepository: EventSignatureRepository,
    private val eventSignatureCachedService: EventSignatureCachedService
) {
    fun getEventSignature(hexSignature: String) =
        eventSignatureCachedService.getEventSignature(hexSignature)

    fun getEventSignatureFilterWithPrimary(hexSignature: String): List<EventSignature> {
        val eventSignatures = mutableListOf<EventSignature>()
        getEventSignature(hexSignature)
            .groupBy { it.textSignature.count { ch -> ch == ',' } }
            .forEach {
                val primaryEventSignature = it.value.find { eventSignature ->  eventSignature.primary == true }
                if(primaryEventSignature != null) {
                    eventSignatures.add(primaryEventSignature)
                } else {
                    eventSignatures.addAll(it.value)
                }
            }
        return eventSignatures
    }

    fun addEventSignature(eventSignature: EventSignature) =
        try {
            eventSignatureCachedService.addEventSignature(eventSignature)
        } catch (dataIntegrityViolationException: DataIntegrityViolationException) {
            eventSignatureRepository.findByFourByteId(eventSignature.fourByteId!!)
        } finally {
            eventSignatureCachedService.flush(eventSignature.hexSignature)
        }

    @Transactional(DbConstants.set1TransactionManager)
    fun updateEventSignaturePrimary(id: Long, primary: Boolean) {
        val eventSignature = eventSignatureRepository.findById(id).orElseGet { null }
        eventSignature?.let {
            eventSignatureCachedService.updateEventSignaturePrimary(it.id, it.hexSignature, primary)
        }
    }
}

@Service
class EventSignatureCachedService(
    private val eventSignatureRepository: EventSignatureRepository
) {
    @Cacheable(cacheNames = [CacheName.EVENT_SIGNATURE], key = "#hexSignature", unless = "#result == null")
    fun getEventSignature(hexSignature: String) =
        eventSignatureRepository.findAllByHexSignature(hexSignature)

    @Transactional(DbConstants.set1TransactionManager)
    fun addEventSignature(eventSignature: EventSignature) =
        eventSignatureRepository.save(eventSignature)

    @CacheEvict(cacheNames = [CacheName.EVENT_SIGNATURE], key = "#hexSignature")
    fun updateEventSignaturePrimary(id: Long, hexSignature: String, primary: Boolean) =
        eventSignatureRepository.updatePrimary(id, primary)

    @CacheEvict(cacheNames = [CacheName.EVENT_SIGNATURE], key = "#hexSignature")
    fun flush(hexSignature: String) {
    }
}