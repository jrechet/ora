package ora.monitoring.consumer.http

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Slf4j
@Service
class RestTemplateHttpClient implements IHttpClient {

    @Autowired
    RestTemplate restTemplate

    String getForString(String url, String headers = null) {
        if (headers) {
            HttpHeaders httpHeaders = new HttpHeaders()
            headers.split('\n').each { header ->
                def (key, value) = header.split(':').collect { it.trim() }
                httpHeaders.add(key, value)
            }
            return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(httpHeaders), String).body
        }
        try {
            return restTemplate.getForObject(url, String)
        } catch (Exception e) {
            log.debug("Erreur lors de la récupération de l'URL: ${url}", e)
            return ''
        }
    }

    /**
     * Vérifie si un service est en bonne santé en vérifiant le code HTTP 200
     * @param url L'URL du service à vérifier
     * @return true si le service répond avec un code HTTP 200, false sinon
     */
    boolean isHealthy(String url) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String)
            return response.statusCode == HttpStatus.OK
        } catch (Exception e) {
            log.debug("Erreur lors de la vérification de la santé pour l'URL: ${url}", e)
            return false
        }
    }

    boolean isAvailable() {
        return true  // RestTemplate est toujours disponible
    }

    void handleResponse(String message) {
        // No-op pour RestTemplate
    }
}