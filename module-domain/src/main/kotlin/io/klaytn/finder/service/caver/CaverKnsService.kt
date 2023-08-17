package io.klaytn.finder.service.caver

import com.klaytn.caver.Caver
import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.infra.cache.CacheName
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.web3j.ens.NameHash

@Service
@EnableConfigurationProperties(KnsProperties::class)
class CaverKnsService(
    private val caver: Caver,
    private val knsProperties: KnsProperties,
) {
    private val logger = logger(this::class.java)
    private val emptyAddress = "0x0000000000000000000000000000000000000000";

    private val registryAbi = this::class.java.classLoader.getResource("caver/abi/kns/KNSRegistry.json")!!.readText()
    private val resolverAbi = this::class.java.classLoader.getResource("caver/abi/kns/PublicResolver.json")!!.readText()
    private val reverseRecordAbi = this::class.java.classLoader.getResource("caver/abi/kns/ReverseRecord.json")!!.readText()

    @Cacheable(cacheNames = [CacheName.ACCOUNT_ADDRESS_BY_KNS], key = "#kns", unless = "#result == null")
    fun getAddress(kns: String): String? {
        if (!knsProperties.enabled) {
            return null
        }

        try {
            val nameHash = NameHash.nameHash(kns)
            val resolverAddress = getResolver(nameHash)
            return resolverAddress?.let {
                getAddress(nameHash, it)
            }
        } catch (exception: Exception) {
            logger.warn(exception.message, exception)
        }
        return null
    }

    fun getName(accountAddress: String): String? {
        if (!knsProperties.enabled) {
            return null
        }

        try {
            val registry = caver.contract.create(reverseRecordAbi, knsProperties.reverseRecordContractAddress)
            val result = registry.call("getName", accountAddress.lowercase())
            return result.firstOrNull()?.toString()
        } catch (exception: Exception) {
            logger.warn(exception.message, exception)
        }
        return null
    }

    private fun getResolver(nameHash: String): String? {
        if (!knsProperties.enabled) {
            return null
        }

        try {
            val registry = caver.contract.create(registryAbi, knsProperties.registryContractAddress)
            val result = registry.call("resolver", nameHash)
            return result.firstOrNull()?.let {
                val resolverAddress = it.toString()
                if (emptyAddress.equals(resolverAddress, ignoreCase = true)) {
                    null
                } else {
                    resolverAddress
                }
            }
        } catch (exception: Exception) {
            logger.warn(exception.message, exception)
        }
        return null
    }

    private fun getAddress(nameHash: String, resolverAddress: String): String? {
        if (!knsProperties.enabled) {
            return null
        }

        try {
            val resolver = caver.contract.create(resolverAbi, resolverAddress)
            val resolverResult = resolver.call("addr", nameHash)
            return resolverResult.firstOrNull()?.let {
                val resolvedAddress = it.toString()
                if (emptyAddress.equals(resolvedAddress, ignoreCase = true)) {
                    null
                } else {
                    resolvedAddress
                }
            }
        } catch (exception: Exception) {
            logger.warn(exception.message, exception)
        }
        return null
    }
}

@ConstructorBinding
@ConfigurationProperties(prefix = "finder.kns")
data class KnsProperties(
    val enabled: Boolean,
    val registryContractAddress: String?,
    val reverseRecordContractAddress: String?,
)