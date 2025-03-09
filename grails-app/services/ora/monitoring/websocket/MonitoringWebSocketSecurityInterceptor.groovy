package ora.monitoring.websocket

import groovy.util.logging.Slf4j
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

@Slf4j
@Component
class MonitoringWebSocketSecurityInterceptor implements HandshakeInterceptor {

    @Override
    boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                          WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        log.info "[DEBUG_LOG] Starting WebSocket security handshake"

        def securityContext = SecurityContextHolder.context
        def authentication = securityContext?.authentication

        log.info "[DEBUG_LOG] Current authentication: ${authentication}"

        // Always propagate the security context if it exists
        if (securityContext) {
            attributes.put("SPRING_SECURITY_CONTEXT", securityContext)
            if (authentication?.principal) {
                attributes.put("user", authentication.principal)
            }
            log.info "[DEBUG_LOG] Security context propagated to WebSocket session"
        } else {
            log.info "[DEBUG_LOG] No security context found, but allowing connection anyway"
            
            // Create an empty security context to prevent "rejectPublicInvocations" errors
            attributes.put("ALLOW_PUBLIC_ACCESS", true)
        }

        // Always allow the handshake to proceed, even without authentication
        return true
    }

    @Override
    void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                       WebSocketHandler wsHandler, Exception exception) {
        if (exception) {
            log.error "[DEBUG_LOG] Handshake failed with exception", exception
        } else {
            log.info "[DEBUG_LOG] Handshake completed successfully"
        }
    }
}
