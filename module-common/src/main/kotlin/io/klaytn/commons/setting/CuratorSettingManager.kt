package io.klaytn.commons.setting

import com.google.common.collect.Maps
import io.klaytn.commons.curator.CuratorTemplate
import io.klaytn.commons.curator.CuratorUtils
import io.klaytn.commons.lifecycle.ApplicationContextInitializedListener
import io.klaytn.commons.setting.transmission.CuratorChangeTransmission
import io.klaytn.commons.utils.logback.logger
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.apache.curator.framework.recipes.cache.ChildData
import org.apache.curator.framework.recipes.cache.CuratorCache
import org.apache.curator.framework.recipes.cache.CuratorCacheListener
import org.apache.curator.utils.CloseableUtils
import org.springframework.beans.factory.DisposableBean
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.concurrent.CustomizableThreadFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.function.Consumer

class CuratorSettingManager(
    private val curatorTemplate: CuratorTemplate,
    private val curatorChangeTransmissions: List<CuratorChangeTransmission>,
    applicationContext: ApplicationContext,
) : ApplicationContextInitializedListener(applicationContext), SettingManager, DisposableBean {
    private val logger = logger(this::class.java)

    private lateinit var curatorCache: CuratorCache

    private val configMap: MutableMap<String, String> = Maps.newConcurrentMap()

    private val executorService: ExecutorService = Executors.newCachedThreadPool(
        CustomizableThreadFactory("curator-listener")
    )

    override fun set(name: String, value: String?): Boolean {
        curatorTemplate.createOrUpdate(name, CuratorUtils.serialize(value))
        return true
    }

    override fun get(name: String): String? {
        return if (configMap[name].isNullOrBlank()) null else configMap[name]
    }

    override fun getBoolean(name: String, defaultValue: Boolean): Boolean {
        val value = get(name)
        return if (StringUtils.isNotBlank(value)) java.lang.Boolean.parseBoolean(value) else defaultValue
    }

    override fun getInteger(name: String, defaultValue: Int): Int {
        val value = get(name)
        return NumberUtils.toInt(value, defaultValue)
    }

    override fun getLong(name: String, defaultValue: Long): Long {
        val value = get(name)
        return NumberUtils.toLong(value, defaultValue)
    }

    fun remove(name: String): Boolean {
        if (!curatorTemplate.exists(name)) {
            return true
        }
        if (curatorTemplate.hasChildren(name)) {
            curatorTemplate.write(name, null)
        } else {
            curatorTemplate.delete(name)
        }
        return true
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -- implements
    // -----------------------------------------------------------------------------------------------------------------

    override fun onApplicationContextInitialized(applicationContext: ApplicationContext) {
        curatorCache = curatorTemplate.createTreeCache(ConfigListener(), executorService)
    }

    override fun destroy() {
        CloseableUtils.closeQuietly(curatorCache)
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -- private
    // -----------------------------------------------------------------------------------------------------------------

    internal inner class ConfigListener : CuratorCacheListener {
        override fun event(type: CuratorCacheListener.Type, oldData: ChildData?, data: ChildData?) {
            val path =
                if (type == CuratorCacheListener.Type.NODE_CREATED) {
                    data!!.path
                } else {
                    oldData!!.path
                }

            val key: String = curatorTemplate.getSubPath(path)
            if(key.isBlank()) {
                return
            }

            val value: String? =
                if (type != CuratorCacheListener.Type.NODE_DELETED) {
                    CuratorUtils.deserialize(data?.data)
                } else {
                    null
                }
            logger.debug("path=$path, key=$key, value=$value")
            value?.let { configMap[key] = value } ?: configMap.remove(key)
            curatorChangeTransmissions.forEach(Consumer { t: CuratorChangeTransmission ->
                t.change(key, value)
            })
        }
    }
}