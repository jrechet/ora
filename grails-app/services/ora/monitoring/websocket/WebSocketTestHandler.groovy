package ora.monitoring.websocket

import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.ClientWebSocket
import io.micronaut.websocket.annotation.OnMessage
import io.micronaut.websocket.annotation.OnOpen

@ClientWebSocket("/ws-endpoint")
class WebSocketTestHandler {
    WebSocketSession session
    String lastReceivedMessage
    boolean connected = false

    @OnOpen
    void onOpen(WebSocketSession session) {
        this.session = session
        this.connected = true
    }

    @OnMessage
    void onMessage(String message) {
        this.lastReceivedMessage = message
    }

    boolean isConnected() {
        return connected
    }
}