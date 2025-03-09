package ora

import ora.monitoring.websocket.MonitoringWebSocketHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean

/**
 * Configuration des WebSockets pour l'application.
 * Cette classe enregistre les handlers WebSocket et configure les endpoints.
 */
@Configuration
@EnableWebSocket
class WebSocketConfig implements WebSocketConfigurer {
    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class)

    @Autowired
    private MonitoringWebSocketHandler monitoringWebSocketHandler

    /**
     * Configure les limites de buffer pour les messages WebSocket
     */
    @Bean
    ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean()
        container.setMaxTextMessageBufferSize(8192)
        container.setMaxBinaryMessageBufferSize(8192)
        container.setMaxSessionIdleTimeout(60000L)
        return container
    }

    @Override
    void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("Registering WebSocket handlers...")
        
        // Configuration simple avec et sans SockJS
        registry.addHandler(monitoringWebSocketHandler, "/monitoring-ws")
                .setAllowedOrigins("*")
        
        registry.addHandler(monitoringWebSocketHandler, "/monitoring-ws-sockjs")
                .setAllowedOrigins("*")
                .withSockJS()
                
        log.info("WebSocket handlers registered successfully")
    }
}
