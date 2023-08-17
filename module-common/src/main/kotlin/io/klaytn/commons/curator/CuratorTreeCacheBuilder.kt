package io.klaytn.commons.curator

import io.klaytn.commons.utils.logback.logger
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.CuratorCache
import org.apache.curator.framework.recipes.cache.CuratorCacheListener
import org.springframework.beans.BeanInstantiationException
import java.util.concurrent.Executor

class CuratorTreeCacheBuilder(
    private val curatorFramework: CuratorFramework,
    private val path: String
) {
    private val logger = logger(this::class.java)
    private val curatorCache = CuratorCache.builder(curatorFramework, path).build()

    fun addListener(listener: CuratorCacheListener): CuratorTreeCacheBuilder {
        curatorCache.listenable().addListener(listener)
        return this
    }

    fun addListener(listener: CuratorCacheListener, executor: Executor): CuratorTreeCacheBuilder {
        curatorCache.listenable().addListener(listener, executor)
        return this
    }

    fun build(): CuratorCache {
        try {
            curatorCache.start()
            logger.debug("path({}) listener is started...", path)
        } catch (e: Exception) {
            throw BeanInstantiationException(this.javaClass, "Couldn't activate a CuratorCache(path=$path)", e)
        }
        return curatorCache
    }
}