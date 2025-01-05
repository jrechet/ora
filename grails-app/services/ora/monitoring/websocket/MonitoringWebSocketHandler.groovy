package ora.monitoring.websocket

import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

@Slf4j
@Component
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
        log.info "New WebSocket connection established: ${session.id}"
        sessions[session.id] = session
        sessionLocks[session.id] = new ReentrantLock()
        sendMessageToSession(session, "Connected to ORA monitoring")
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
            log.warn "No lock found for session ${session.id}"
            return
        }

        lock.lock()
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message))
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