package ora.monitoring.consumer.supervision

import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service

@Slf4j
@Service
class SupervisionClientDelegateService {

    /**
     * Parse le HTML de supervision et retourne le résultat avec les services en erreur
     * @param html le contenu HTML de la page de supervision
     * @return SupervisionResult contenant le statut et la liste des services en erreur
     */
    SupervisionResult parse(String html) {
        def result = new SupervisionResult(success: true)

        if (!html) {
            log.warn("HTML de supervision vide")
            return result
        }

        try {
            // On extrait le corps de la table en utilisant une regex
            def tableBodyPattern = /(?s)<tbody>(.*?)<\/tbody>/
            def matcher = (html =~ tableBodyPattern)

            if (!matcher.find()) {
                log.warn("Aucun corps de table trouvé dans le HTML de supervision")
                return result
            }

            def tableBody = matcher.group(1)

            // On extrait toutes les lignes de vérification
            def rowPattern = /(?s)<tr id="V\d+">(.*?)<\/tr>/
            def rows = (tableBody =~ rowPattern)

            if (!rows) {
                log.warn("Aucune ligne de vérification trouvée dans le HTML de supervision")
                return result
            }

            // Pour chaque ligne, on vérifie si le statut est OK
            def allOk = true
            rows.each { fullMatch, rowContent ->
                def servicePattern = /<td>V\d+&nbsp;:&nbsp;(.*?)<\/td>/
                def statusPattern = /<td class="(\w+)">(\w+)<\/td>/

                def serviceMatcher = (rowContent =~ servicePattern)
                def statusMatcher = (rowContent =~ statusPattern)

                if (statusMatcher.find()) {
                    def status = statusMatcher.group(2)
                    if (status.toUpperCase() != "OK") {
                        allOk = false
                        // Si service non OK, on récupère son nom
                        if (serviceMatcher.find()) {
                            def serviceText = serviceMatcher.group(1)
                                    .replaceAll('&nbsp;', ' ')
                                    .split('=')[0]
                                    .replaceAll('url', '')
                            result.failedServices << serviceText
                        }
                    }
                }
            }

            result.success = allOk
            return result

        } catch (Exception e) {
            log.error("Erreur lors du parsing du HTML de supervision", e)
            return result
        }
    }
}