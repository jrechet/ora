package ora.monitoring.websocket

import grails.converters.JSON
import grails.core.GrailsApplication
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import ora.monitoring.consumer.HttpException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Slf4j
@Service
class WebSocketHttpClientService {

    @Autowired
    private ApplicationContext applicationContext

    @Autowired
    GrailsApplication grailsApplication

    private final Map<String, CompletableFuture<Map>> pendingRequests = new ConcurrentHashMap<>()

    private MonitoringWebSocketHandler getHandler() {
        return applicationContext.getBean(MonitoringWebSocketHandler)
    }

    String getForString(String url, String headers = null) {
        def response = doHttpRequest([url: url, headers: headers])
        if (!response) {
            return ''
        }

        if (response.status != 200) {
            return ''
        }

        if (response.data == null) {
            return ''
        }

        return response.data.toString()
    }
    
    /**
     * Vérifie si un service est en bonne santé en vérifiant le code HTTP 200
     * @param url L'URL du service à vérifier
     * @return true si le service répond avec un code HTTP 200, false sinon
     */
    boolean isHealthy(String url) {
        try {
            def response = doHttpRequest([url: url])
            if (!response) {
                log.debug("Réponse vide lors de la vérification de la santé pour l'URL: ${url}")
                return false
            }
            
            boolean isHealthy = response.status == 200
            if (!isHealthy) {
                log.debug("Le service à l'URL ${url} a répondu avec le code HTTP: ${response.status}")
            }
            
            return isHealthy
        } catch (Exception e) {
            log.debug("Erreur lors de la vérification de la santé pour l'URL: ${url}", e)
            return false
        }
    }

    private Map doHttpRequest(Map request) {
        if (!handler.hasActiveSessions()) {
            log.warn "No WebSocket sessions available for HTTP request"
            return [:]
        }

        String requestId = UUID.randomUUID().toString()
        CompletableFuture<Map> future = new CompletableFuture<>()
        pendingRequests[requestId] = future

        try {
            def command = [
                    type     : 'HTTP_REQUEST',
                    requestId: requestId,
                    url      : request.url
            ]
            if (request.headers) {
                command.headers = request.headers
            }

            handler.sendMessage(JsonOutput.toJson(command))

            def result = future.get(30, TimeUnit.SECONDS)

            if (result.error) {
                throw new HttpException(result.error)
            }

            return result

        } catch (Exception e) {
            log.error "Error doing HTTP request via WebSocket: ${e.message}"
            throw new HttpException("WebSocket request failed: ${e.message}")
        } finally {
            pendingRequests.remove(requestId)
        }
    }

    void handleResponse(String message) {
        try {
            if (!message) {
                log.warn "Received empty WebSocket message"
                return
            }

            def payload = JSON.parse(message)
            if (!payload || !payload.type) {
                log.warn "Invalid WebSocket message format: ${message}"
                return
            }

            if (payload.type == 'HTTP_RESPONSE') {
                def future = pendingRequests.remove(payload.requestId)
                if (future) {
                    future.complete([
                            status: payload.status ?: 0,
                            data  : payload.data,
                            error : payload.error
                    ])
                } else {
                    log.warn "No pending request found for ID: ${payload.requestId}"
                }
            }
        } catch (Exception e) {
            log.error "Error handling WebSocket response: ${e.message}", e
        }
    }

    boolean isAvailable() {
        return handler.hasActiveSessions()
    }
}