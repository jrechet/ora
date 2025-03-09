package ora.monitoring.websocket

import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

@Slf4j
@Service
class MonitoringWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>()
    private final Map<String, ReentrantLock> sessionLocks = new ConcurrentHashMap<>()
    private final WebSocketHttpClientService webSocketHttpClientService

    MonitoringWebSocketHandler(WebSocketHttpClientService webSocketHttpClientService) {
        this.webSocketHttpClientService = webSocketHttpClientService
    }

    boolean hasActiveSessions() {
        return sessions.values().any { it.isOpen() }
    }

    @Override
    void afterConnectionEstablished(WebSocketSession session) {
        log.info "[DEBUG_LOG] New WebSocket connection established: ${session.id}"
        log.info "[DEBUG_LOG] Session attributes: ${session.attributes}"
        log.info "[DEBUG_LOG] Connection headers: ${session.handshakeHeaders}"
        log.info "[DEBUG_LOG] URI: ${session.uri}"

        // Accept all connections without checking authentication
        log.info "[DEBUG_LOG] Connection accepted unconditionally"
        sessions[session.id] = session
        sessionLocks[session.id] = new ReentrantLock()
        
        try {
            // Envoyer un message de bienvenue
            sendMessageToSession(session, "Connected to ORA monitoring")
        } catch (Exception e) {
            log.error "[DEBUG_LOG] Error sending welcome message", e
        }
    }

    @Override
    void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info "WebSocket connection closed: ${session.id} with status ${status}"
        sessions.remove(session.id)
        sessionLocks.remove(session.id)
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            log.debug "Received message: ${message.payload}"
            webSocketHttpClientService.handleResponse(message.payload)
        } catch (Exception e) {
            log.error "Error handling message: ${e.message}", e
        }
    }

    @Override
    void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error "Transport error in session ${session.id}", exception
        sessions.remove(session.id)
        sessionLocks.remove(session.id)
    }

    void sendMessage(String message) {
        def failedSessions = []

        sessions.each { String id, WebSocketSession session ->
            try {
                if (session.isOpen()) {
                    sendMessageToSession(session, message)
                } else {
                    failedSessions << id
                }
            } catch (Exception e) {
                log.error "Error sending message to session ${id}", e
                failedSessions << id
            }
        }

        failedSessions.each { id ->
            sessions.remove(id)
            sessionLocks.remove(id)
        }
    }

    private void sendMessageToSession(WebSocketSession session, String message) {
        def lock = sessionLocks.get(session.id)
        if (!lock) {
            log.warn "No lock found for session ${session.id}, creating one"
            lock = new ReentrantLock()
            sessionLocks[session.id] = lock
        }

        lock.lock()
        try {
            if (session.isOpen()) {
                try {
                    log.debug "Sending message to session ${session.id}: ${message}"
                    session.sendMessage(new TextMessage(message))
                    log.debug "Message sent successfully"
                } catch (IOException e) {
                    log.error "IO error sending message to session ${session.id}", e
                } catch (Exception e) {
                    log.error "Error sending message to session ${session.id}", e 
                }
            } else {
                log.warn "Cannot send message to session ${session.id} - session closed"
            }
        } finally {
            lock.unlock()
        }
    }

    void sendMessageToSpecificSession(String sessionId, String message) {
        def session = sessions.get(sessionId)
        if (session && session.isOpen()) {
            sendMessageToSession(session, message)
        }
    }
}
