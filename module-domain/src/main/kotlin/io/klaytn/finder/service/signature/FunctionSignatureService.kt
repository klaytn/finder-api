package io.klaytn.finder.service.signature

import io.klaytn.finder.domain.mysql.set1.signature.FunctionSignature
import io.klaytn.finder.domain.mysql.set1.signature.FunctionSignatureRepository
import io.klaytn.finder.infra.cache.CacheName
import io.klaytn.finder.infra.db.DbConstants
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FunctionSignatureService(
    private val functionSignatureRepository: FunctionSignatureRepository,
    private val functionSignatureCachedService: FunctionSignatureCachedService
) {
    fun getFunctionSignature(bytesSignature: String) =
        functionSignatureCachedService.getFunctionSignature(bytesSignature)

    fun getFunctionSignatureFilterWithPrimary(bytesSignature: String): List<FunctionSignature> {
        val functionSignatures = mutableListOf<FunctionSignature>()
        getFunctionSignature(bytesSignature)
            .groupBy { it.textSignature.count { ch -> ch == ',' } }
            .forEach {
                val primaryFunctionSignature = it.value.find { functionSignature ->  functionSignature.primary == true }
                if(primaryFunctionSignature != null) {
                    functionSignatures.add(primaryFunctionSignature)
                } else {
                    functionSignatures.addAll(it.value)
                }
            }
        return functionSignatures
    }

    fun getFunctionSignaturesFilterWithPrimary(bytesSignature: List<String>): Map<String, List<FunctionSignature>> {
        val functionSignatures = mutableMapOf<String, List<FunctionSignature>>()
        getFunctionSignatures(bytesSignature).groupBy { it.bytesSignature to it}.forEach{
            val sig = it.key.first
            it.value.groupBy { it.textSignature.count { ch -> ch == ',' } }.forEach {
                val primaryFunctionSignature = it.value.find { functionSignature ->  functionSignature.primary == true }
                if(primaryFunctionSignature != null) {
                    functionSignatures[sig] = functionSignatures[sig]?.plus(primaryFunctionSignature) ?: listOf(primaryFunctionSignature)
                } else {
                    functionSignatures[sig] = functionSignatures[sig]?.plus(it.value) ?: it.value
                }
            }
        }
        return functionSignatures
    }

    fun getFunctionSignatures(bytes: List<String>) =
        functionSignatureRepository.findAllByBytesSignatureIn(bytes)

    fun addFunctionSignature(functionSignature: FunctionSignature) =
        try {
            functionSignatureCachedService.addFunctionSignature(functionSignature)
        } catch (dataIntegrityViolationException: DataIntegrityViolationException) {
            functionSignatureRepository.findByFourByteId(functionSignature.fourByteId!!)
        } finally {
            functionSignatureCachedService.flush(functionSignature.bytesSignature)
        }

    @Transactional(DbConstants.set1TransactionManager)
    fun updateFunctionSignaturePrimary(id: Long, primary: Boolean) {
        val functionSignature = functionSignatureRepository.findById(id).orElseGet { null }
        functionSignature?.let {
            functionSignatureCachedService.updateFunctionSignaturePrimary(it.id, it.bytesSignature, primary)
        }
    }
}

@Service
class FunctionSignatureCachedService(
    private val functionSignatureRepository: FunctionSignatureRepository,
) {
    @Cacheable(cacheNames = [CacheName.FUNCTION_SIGNATURE], key = "#bytesSignature", unless = "#result == null")
    fun getFunctionSignature(bytesSignature: String) =
        functionSignatureRepository.findAllByBytesSignature(bytesSignature)

    @Transactional(DbConstants.set1TransactionManager)
    fun addFunctionSignature(functionSignature: FunctionSignature) =
        functionSignatureRepository.save(functionSignature)

    @CacheEvict(cacheNames = [CacheName.FUNCTION_SIGNATURE], key = "#bytesSignature")
    fun updateFunctionSignaturePrimary(id: Long, bytesSignature: String, primary: Boolean) =
        functionSignatureRepository.updatePrimary(id, primary)

    @CacheEvict(cacheNames = [CacheName.FUNCTION_SIGNATURE], key = "#bytesSignature")
    fun flush(bytesSignature: String) {
    }
}