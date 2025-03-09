package ora.monitoring.websocket

import grails.testing.web.GrailsWebUnitTest
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import spock.lang.Specification

/**
 * Test de connexion WebSocket pour assurer que la configuration est correcte.
 */
class WebSocketConnectionSpec extends Specification implements GrailsWebUnitTest {

    MonitoringWebSocketHandler webSocketHandler
    
    def setup() {
        def mockWebSocketHttpClientService = Mock(WebSocketHttpClientService)
        webSocketHandler = new MonitoringWebSocketHandler(mockWebSocketHttpClientService)
    }
    
    def "test WebSocket connection establishment"() {
        given: "a mock WebSocket session"
        def mockSession = Mock(WebSocketSession) {
            getId() >> "test-session-id"
            isOpen() >> true
            getAttributes() >> [:]
        }
        
        when: "a connection is established"
        webSocketHandler.afterConnectionEstablished(mockSession)
        
        then: "the session should be stored and a message sent"
        webSocketHandler.hasActiveSessions() == true
        1 * mockSession.sendMessage(_) >> { arguments ->
            def message = arguments[0]
            assert message instanceof TextMessage
            assert message.payload == "Connected to ORA monitoring"
        }
    }
    
    def "test WebSocket message handling"() {
        given: "a mock WebSocket session and client service"
        def mockSession = Mock(WebSocketSession) {
            getId() >> "test-session-id"
            isOpen() >> true
            getAttributes() >> [:]
        }
        def mockMessage = new TextMessage('{"type":"test","data":"test-data"}')
        
        and: "the session is connected"
        webSocketHandler.afterConnectionEstablished(mockSession)
        
        when: "a message is received"
        webSocketHandler.handleTextMessage(mockSession, mockMessage)
        
        then: "the message should be handled by the client service"
        1 * webSocketHandler.webSocketHttpClientService.handleResponse(mockMessage.payload)
    }
    
    def "test WebSocket connection closing"() {
        given: "a mock WebSocket session that was connected"
        def mockSession = Mock(WebSocketSession) {
            getId() >> "test-session-id"
            isOpen() >> true
            getAttributes() >> [:]
        }
        webSocketHandler.afterConnectionEstablished(mockSession)
        
        when: "the connection is closed"
        webSocketHandler.afterConnectionClosed(mockSession, CloseStatus.NORMAL)
        
        then: "the session should be removed"
        webSocketHandler.hasActiveSessions() == false
    }
    
    def "test WebSocket broadcast message"() {
        given: "multiple mock WebSocket sessions"
        def mockSession1 = Mock(WebSocketSession) {
            getId() >> "test-session-1"
            isOpen() >> true
            getAttributes() >> [:]
        }
        def mockSession2 = Mock(WebSocketSession) {
            getId() >> "test-session-2"
            isOpen() >> true
            getAttributes() >> [:]
        }
        
        and: "the sessions are connected"
        webSocketHandler.afterConnectionEstablished(mockSession1)
        webSocketHandler.afterConnectionEstablished(mockSession2)
        
        when: "a broadcast message is sent"
        webSocketHandler.sendMessage("Test broadcast message")
        
        then: "all sessions should receive the message"
        1 * mockSession1.sendMessage(_) >> { arguments ->
            def message = arguments[0]
            assert message instanceof TextMessage
            assert message.payload == "Test broadcast message"
        }
        1 * mockSession2.sendMessage(_) >> { arguments ->
            def message = arguments[0]
            assert message instanceof TextMessage
            assert message.payload == "Test broadcast message"
        }
    }
}