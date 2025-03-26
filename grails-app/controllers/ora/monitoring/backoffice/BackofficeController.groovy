package ora.monitoring.backoffice

import grails.plugin.springsecurity.annotation.Secured

/**
 * Contrôleur pour gérer les pages du backoffice.
 * Ce contrôleur fournit les actions de base pour accéder
 * au backoffice de l'application.
 */
@Secured(['ROLE_ADMIN'])
class BackofficeController {

    /**
     * Action par défaut qui affiche la page d'accueil du backoffice
     * @return Map contenant les données pour la vue
     */
    def index() {
        // Données pour la page d'accueil du backoffice
        [
            pageTitle: "Tableau de bord",
            currentSection: "alertPreferences"
        ]
        render view: '/alertPreference/index'
    }
}
