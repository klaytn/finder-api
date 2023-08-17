package io.klaytn.finder.config

import io.klaytn.finder.infra.ServerMode
import io.klaytn.finder.interfaces.rest.api.websocket.FinderHomeHandshakeAuthInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Profile(ServerMode.API_MODE)
@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val finderWebConfig: FinderWebConfig,
    private val finderHomeHandshakeAuthInterceptor: FinderHomeHandshakeAuthInterceptor,
) : WebSocketMessageBrokerConfigurer {
    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // TODO: Remove this comment after frontend deployment
        registry.addEndpoint("/ws")
//            .addInterceptors(finderHomeHandshakeAuthInterceptor)
            .setAllowedOriginPatterns(*finderWebConfig.allowedOriginPatterns.toTypedArray())
    }
}
