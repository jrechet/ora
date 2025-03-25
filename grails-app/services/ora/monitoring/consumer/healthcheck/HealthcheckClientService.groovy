package ora.monitoring.consumer.healthcheck

import groovy.util.logging.Slf4j
import ora.monitoring.consumer.http.HttpClientDelegateService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Slf4j
@Service
class HealthcheckClientService {

    @Autowired
    HttpClientDelegateService httpClient

    boolean checkHealth(String healthUrl) {
        if (!healthUrl) {
            return false
        }

        try {
            log.debug "Vérification du healthcheck pour l'URL: ${healthUrl}"
            boolean isHealthy = httpClient.isHealthy(healthUrl)
            
            if (!isHealthy) {
                log.warn("Le service à l'URL ${healthUrl} n'est pas en bonne santé")
                return false
            }

            return true
        } catch (Exception e) {
            log.trace("Erreur lors de la vérification du healthcheck pour l'URL: ${healthUrl}", e.message)
            return false
        }
    }
}