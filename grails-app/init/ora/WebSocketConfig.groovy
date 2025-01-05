package ora

import ora.monitoring.websocket.MonitoringWebSocketHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig implements WebSocketConfigurer {

    private final MonitoringWebSocketHandler monitoringWebSocketHandler

    WebSocketConfig(MonitoringWebSocketHandler monitoringWebSocketHandler) {
        this.monitoringWebSocketHandler = monitoringWebSocketHandler
    }

    @Override
    void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(monitoringWebSocketHandler, "/monitoring-ws")
                .setAllowedOrigins("*")
    }
}