package ora.monitoring.consumer.supervision

import groovy.util.logging.Slf4j
import ora.monitoring.consumer.http.HttpClientDelegateService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Slf4j
@Service
class SupervisionClientService {

    @Autowired
    HttpClientDelegateService httpClient

    @Autowired
    SupervisionClientDelegateService supervisionClientDelegateService

    SupervisionResult checkSupervision(String url) {
        if (!url) {
            return new SupervisionResult()
        }

        try {
            // On suppose que webSocketHttpClientService.getForString retourne maintenant juste la réponse HTTP en String
            String response = httpClient.getForString(url)

            // Si la réponse est null ou vide, on considère que c'est un échec
            if (!response) {
                log.warn("Réponse vide de l'URL de supervision")
                return new SupervisionResult(success: false)
            }

            return supervisionClientDelegateService.parse(response)
        } catch (Exception e) {
            log.error("Erreur lors de l'appel à l'URL de supervision: ${url}", e)
            return new SupervisionResult(success: false)
        }
    }
}