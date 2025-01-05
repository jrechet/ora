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
            String response = httpClient.getForString(healthUrl)

            if (!response) {
                log.warn("Réponse vide du healthcheck")
                return false
            }

            return true
        } catch (Exception e) {
            log.trace("Erreur lors de la vérification du healthcheck pour l'URL: ${healthUrl}", e.message)
            return false
        }
    }
}