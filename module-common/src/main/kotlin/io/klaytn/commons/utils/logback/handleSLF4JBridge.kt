package io.klaytn.commons.utils.logback

import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler

fun handleSLF4JBridge() {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
}

fun <T> logger(clazz: Class<T>) = LoggerFactory.getLogger(clazz)

fun logger(clazz: String) = LoggerFactory.getLogger(clazz)
