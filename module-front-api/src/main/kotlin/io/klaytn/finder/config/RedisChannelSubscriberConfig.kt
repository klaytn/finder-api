package io.klaytn.finder.config

import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.infra.redis.ChannelSubscriber
import io.klaytn.finder.infra.redis.RedisKeyManager
import io.klaytn.finder.interfaces.rest.api.websocket.FinderHomePublisher
import io.klaytn.finder.service.BlockService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter

@Profile(ServerMode.API_MODE)
@Configuration
class RedisChannelSubscriberConfig(
    private val redisConnectionFactory: RedisConnectionFactory,
    private val redisKeyManager: RedisKeyManager,
) {
    @Bean
    fun channelListenerContainer(channelSubscriber: ChannelSubscriber) =
        RedisMessageListenerContainer().apply {
            setConnectionFactory(redisConnectionFactory)
            addMessageListener(MessageListenerAdapter(channelSubscriber),
                listOf(
                    PatternTopic.of(redisKeyManager.chainCommonChannelPattern),
                    PatternTopic.of(redisKeyManager.chainChannelPattern)
                ))
        }

    @Bean
    fun channelSubscriber(
        blockService: BlockService,
        finderHomePublisher: FinderHomePublisher,
        redisTemplate: RedisTemplate<String, String>,
        redisKeyManager: RedisKeyManager
    ) =
        ChannelSubscriber(blockService, finderHomePublisher, redisTemplate, redisKeyManager)
}
