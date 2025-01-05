package ora.monitoring.websocket

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.AbstractWebSocketHandler
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import javax.websocket.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Integration
@Rollback
class MonitoringWebSocketServerIntegrationSpec extends Specification {

    @Shared
    String baseUrl

    WebSocketSession session
    def conditions = new PollingConditions(timeout: 10)
    TestWebSocketHandler handler = new TestWebSocketHandler()


    TestWebSocketClient wsClient


    def setup() {
        baseUrl = "ws://localhost:${serverPort}"
        wsClient = new TestWebSocketClient()
        println "Server port: ${baseUrl}"

    }

    def cleanup() {
        if (session?.isOpen()) {
            session.close()
        }
    }

    def "should successfully establish WebSocket connection"() {
        given: "a WebSocket endpoint URL "
        def wsUrl = "${baseUrl}/monitoring-ws"

        when: "connecting to the WebSocket endpoint"
        def connected = wsClient.connect(wsUrl)

        then: "the connection should be established"
        connected

        and: "should receive a welcome message"
        conditions.eventually {
            wsClient.messages.size() > 0
            wsClient.messages.first() == "Connected to ORA monitoring"
        }
    }
}

class TestWebSocketHandler extends AbstractWebSocketHandler {
    String lastMessage

    @Override
    void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        super.handleMessage(session, message)
    }
}

@ClientEndpoint
class TestWebSocketClient {
    private Session session
    private final List<String> messages = Collections.synchronizedList([])
    private final CountDownLatch connectLatch = new CountDownLatch(1)

    List<String> getMessages() {
        return new ArrayList<>(messages)
    }

    boolean connect(String wsUrl) {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer()
        try {
            session = container.connectToServer(this, new URI(wsUrl))
            return connectLatch.await(5, TimeUnit.SECONDS)
        } catch (Exception e) {
            e.printStackTrace()
            return false
        }
    }

    void sendMessage(String message) {
        session?.basicRemote?.sendText(message)
    }

    void close() {
        session?.close()
    }

    @OnOpen
    void onOpen(Session session) {
        this.session = session
        connectLatch.countDown()
    }

    @OnMessage
    void onMessage(String message) {
        messages.add(message)
    }

    @OnClose
    void onClose(Session session, CloseReason reason) {
        this.session = null
    }

    @OnError
    void onError(Session session, Throwable throwable) {
        throwable.printStackTrace()
    }
}