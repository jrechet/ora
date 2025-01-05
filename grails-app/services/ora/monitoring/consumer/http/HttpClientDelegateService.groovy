package ora.monitoring.consumer.http

import grails.core.GrailsApplication
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util.concurrent.atomic.AtomicReference

@Slf4j
@Service
class HttpClientDelegateService {

    @Autowired
    GrailsApplication grailsApplication

    @Autowired
    RestTemplateHttpClient restTemplateClient

    @Autowired
    WebSocketHttpClient webSocketClient

    public final AtomicReference<String> currentMode = new AtomicReference<>('server')

    private HttpClient getActiveClient() {
        def mode = currentMode.get()

        if (mode == 'websocket') {
            if (webSocketClient.isAvailable()) {
                return webSocketClient
            }
            log.warn "WebSocket client not available, falling back to REST client"
        }
        return restTemplateClient
    }

    String getForString(String url, String headers = null) {
        return getActiveClient().getForString(url, headers)
    }

    void setMode(String mode) {
        if (!['autonomous', 'websocket', 'server'].contains(mode)) {
            throw new IllegalArgumentException("Invalid mode: ${mode}")
        }
        currentMode.set(mode)
        log.info "Monitoring mode set to: ${mode}"
    }

    String getMode() {
        return currentMode.get()
    }
}