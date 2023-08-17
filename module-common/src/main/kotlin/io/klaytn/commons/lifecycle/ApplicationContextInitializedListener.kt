package io.klaytn.commons.lifecycle

import org.springframework.context.ApplicationContext
import org.springframework.context.SmartLifecycle

abstract class ApplicationContextInitializedListener(
        private val applicationContext: ApplicationContext
) : SmartLifecycle {

    @Volatile private var initialized = false

    @Volatile private var running = false

    override fun start() {
        if (running) {
            return
        }
        if (!initialized) {
            onApplicationContextInitialized(applicationContext)
            initialized = true
        }
        running = true
    }

    override fun stop() {
        running = false
    }

    override fun isRunning(): Boolean {
        return running
    }

    protected abstract fun onApplicationContextInitialized(applicationContext: ApplicationContext)
}
