package io.klaytn.commons.redis.consumer

import java.util.function.Consumer
import org.springframework.context.SmartLifecycle

class RedisConsumerListenerManager(
        private val redisConsumerListeners: List<RedisConsumerListener>,
) : SmartLifecycle {

    private var running = false

    override fun start() {
        checkAndReload(true)
        running = true
    }

    override fun stop() {
        checkAndReload(false)
        running = false
    }

    override fun isRunning(): Boolean {
        return running
    }

    private fun checkAndReload(isOn: Boolean) =
            redisConsumerListeners.forEach(
                    Consumer { redisConsumerListener: RedisConsumerListener ->
                        if (isOn) {
                            redisConsumerListener.start()
                        } else {
                            redisConsumerListener.stop()
                        }
                    }
            )
}
