package io.klaytn.finder.config.dynamic

import com.google.common.collect.Maps
import io.klaytn.commons.setting.transmission.CuratorChangeTransmission
import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.config.ApplicationProperty
import org.apache.commons.lang3.math.NumberUtils
import org.springframework.stereotype.Component
import java.util.Objects

@Component
class FinderClientDynamicConfig(
    private val applicationProperty: ApplicationProperty,
) : CuratorChangeTransmission {
    private val logger = logger(this::class.java)

    private val rootPath = applicationProperty.getApplicationDynamicConfigPath("client/features/")

    val configMap: MutableMap<String, Any> = Maps.newConcurrentMap()

    override fun change(key: String, value: String?) {
        val internalKey: String? = if(key.startsWith(rootPath)) key.substringAfter(rootPath) else null
        if(internalKey.isNullOrBlank()) {
            return
        }
        value?.let {
            val originalValue = configMap[internalKey]
            if(it.equals("true", true) || it.equals("false", true)) {
                configMap[internalKey] = it.toBoolean()
            } else if( NumberUtils.isDigits(it)) {
                configMap[internalKey] = it.toLong()
            } else {
                configMap[internalKey] = it
            }

            val newValue = configMap[internalKey]
            if(!Objects.equals(newValue, originalValue)) {
                logger.warn(
                    "Target Field('{}') of Path('{}') is changed. {} => {}", internalKey, key, originalValue, newValue
                )
            }
        } ?: configMap.remove(internalKey).apply {
            logger.warn("Target Field('{}') of Path('{}') is removed.", internalKey, key)
        }
    }
}