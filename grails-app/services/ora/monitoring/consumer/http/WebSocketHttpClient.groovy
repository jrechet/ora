package ora.monitoring.consumer.http

import ora.monitoring.websocket.WebSocketHttpClientService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class WebSocketHttpClient implements HttpClient {

    @Autowired
    WebSocketHttpClientService webSocketHttpClientService

    String getForString(String url, String headers = null) {
        return webSocketHttpClientService.getForString(url, headers)
    }

    boolean isAvailable() {
        return webSocketHttpClientService.isAvailable()
    }

    void handleResponse(String message) {
        webSocketHttpClientService.handleResponse(message)
    }
}