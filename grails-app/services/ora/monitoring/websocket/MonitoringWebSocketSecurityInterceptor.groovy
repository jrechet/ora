package ora.monitoring.websocket

import groovy.util.logging.Slf4j
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

/**
 * Intercepteur de sécurité minimal pour les WebSockets.
 * Permet toutes les connexions sans vérifier l'authentification.
 */
@Slf4j
@Component
class MonitoringWebSocketSecurityInterceptor implements HandshakeInterceptor {

    @Override
    boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                          WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        log.debug "WebSocket handshake started"
        
        // Toujours permettre les connexions, même sans authentification
        return true
    }

    @Override
    void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                       WebSocketHandler wsHandler, Exception exception) {
        if (exception) {
            log.error "WebSocket handshake failed", exception
        } else {
            log.debug "WebSocket handshake completed"
        }
    }
}
