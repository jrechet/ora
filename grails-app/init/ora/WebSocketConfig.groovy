package ora

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import ora.monitoring.websocket.MonitoringWebSocketHandler

/**
 * Configuration unique pour les WebSockets et la sécurité.
 * Cette classe simple se charge uniquement de l'essentiel.
 */
@Configuration
@EnableWebSocket
class WebSocketConfig implements WebSocketConfigurer {
    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class)

    @Autowired
    private MonitoringWebSocketHandler monitoringWebSocketHandler

    /**
     * Fournit un PasswordEncoder pour Spring Security.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder()
    }

    @Override
    void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("Registering WebSocket handlers...")
        
        // Configuration WebSocket simplifiée à l'extrême
        registry.addHandler(monitoringWebSocketHandler, "/ws-endpoint")
                .setAllowedOrigins("*")
        
        log.info("WebSocket handlers registered successfully")
    }
}
