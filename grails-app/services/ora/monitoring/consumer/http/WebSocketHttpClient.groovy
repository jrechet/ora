package ora.monitoring.consumer.http

import groovy.util.logging.Slf4j
import ora.monitoring.websocket.WebSocketHttpClientService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Slf4j
@Service
class WebSocketHttpClient implements IHttpClient {

    @Autowired
    WebSocketHttpClientService webSocketHttpClientService

    String getForString(String url, String headers = null) {
        return webSocketHttpClientService.getForString(url, headers)
    }
    
    /**
     * Vérifie si un service est en bonne santé en vérifiant le code HTTP 200
     * @param url L'URL du service à vérifier
     * @return true si le service répond avec un code HTTP 200, false sinon
     */
    boolean isHealthy(String url) {
        try {
            return webSocketHttpClientService.isHealthy(url)
        } catch (Exception e) {
            log.debug("Erreur lors de la vérification de la santé pour l'URL: ${url} via WebSocket", e)
            return false
        }
    }

    boolean isAvailable() {
        return webSocketHttpClientService.isAvailable()
    }

    void handleResponse(String message) {
        webSocketHttpClientService.handleResponse(message)
    }
}