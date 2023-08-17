package io.klaytn.commons.curator

import io.klaytn.commons.utils.logback.logger
import org.apache.commons.lang3.StringUtils
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.CuratorCache
import org.apache.curator.framework.recipes.cache.CuratorCacheListener
import org.apache.zookeeper.CreateMode
import org.apache.zookeeper.KeeperException.NodeExistsException
import org.springframework.util.CollectionUtils
import java.nio.file.Paths
import java.util.concurrent.ExecutorService

class CuratorTemplate(
    private val curatorFramework: CuratorFramework,
    private val rootPath: String
) {
    private val logger = logger(this::class.java)

    fun createOrUpdate(path: String, bytes: ByteArray?) {
        val fullPath = getFullPath(path)
        try {
            curatorFramework.create().creatingParentsIfNeeded().forPath(fullPath, bytes)
        } catch (ignored: NodeExistsException) {
            write(path, bytes)
        } catch (e: Exception) {
            logger.warn(e.message, e)
        }
    }

    fun createOrUpdate(path: String, createMode: CreateMode, bytes: ByteArray): String? {
        val fullPath = getFullPath(path)
        try {
            return curatorFramework.create()
                .creatingParentContainersIfNeeded()
                .withMode(createMode)
                .forPath(fullPath, bytes)
        } catch (ignored: NodeExistsException) {
            write(path, bytes)
        } catch (e: Exception) {
            logger.warn(e.message, e)
        }
        return null
    }

    fun exists(path: String): Boolean {
        try {
            val fullPath = getFullPath(path)
            return curatorFramework.checkExists().forPath(fullPath) != null
        } catch (e: Exception) {
            logger.warn(e.message, e)
        }
        return false
    }

    fun read(path: String): ByteArray? {
        try {
            val fullPath = getFullPath(path)
            return curatorFramework.data.forPath(fullPath)
        } catch (e: Exception) {
            logger.warn(e.message, e)
        }
        return null
    }

    fun write(path: String, bytes: ByteArray?) {
        try {
            val fullPath = getFullPath(path)
            curatorFramework.setData().forPath(fullPath, bytes)
        } catch (e: Exception) {
            logger.warn(e.message, e)
        }
    }

    fun delete(path: String) {
        try {
            val fullPath = getFullPath(path)
            curatorFramework.delete().forPath(fullPath)
        } catch (e: Exception) {
            logger.warn(e.message, e)
        }
    }

    fun getChildren(path: String): List<String> {
        try {
            val fullPath = getFullPath(path)
            return curatorFramework.children.forPath(fullPath)
        } catch (e: Exception) {
            logger.warn(e.message, e)
        }
        return emptyList()
    }

    fun hasChildren(path: String): Boolean {
        return !CollectionUtils.isEmpty(getChildren(path))
    }

    fun createTreeCache(treeCacheListener: CuratorCacheListener, executorService: ExecutorService): CuratorCache {
        return CuratorTreeCacheBuilder(curatorFramework, rootPath)
            .addListener(treeCacheListener, executorService).build()
    }

    fun getFullPath(subPath: String): String {
        return Paths.get(rootPath, subPath).toString()
    }

    fun getSubPath(fullPath: String): String {
        return StringUtils.substringAfter(fullPath, rootPath + CuratorConstants.PATH_SEPARATOR)
    }
}