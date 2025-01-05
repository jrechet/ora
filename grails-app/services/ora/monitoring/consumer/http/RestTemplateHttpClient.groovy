package ora.monitoring.consumer.http

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class RestTemplateHttpClient implements HttpClient {

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
            return ''
        }
    }

    boolean isAvailable() {
        return true  // RestTemplate est toujours disponible
    }

    void handleResponse(String message) {
        // No-op pour RestTemplate
    }
}