package ora.monitoring.alerts

import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import org.springframework.http.HttpStatus

/**
 * Contrôleur pour gérer les préférences d'alerte.
 * Ce contrôleur fournit les actions nécessaires pour afficher
 * et mettre à jour les préférences d'alerte de l'utilisateur connecté.
 */
@Secured(['ROLE_ADMIN', 'ROLE_USER'])
class AlertPreferenceController {

    AlertPreferenceService alertPreferenceService

    static allowedMethods = [
        index: "GET",
        update: "POST"
    ]

    /**
     * Affiche les préférences d'alerte de l'utilisateur connecté
     * @return Map contenant les données pour la vue
     */
    def index() {
        def preference = alertPreferenceService.getPreference()
        if (!preference) {
            preference = alertPreferenceService.createDefaultPreference()
        }
        
        [
            preference: preference,
            pageTitle: "Préférences d'alerte",
            currentSection: "alertPreferences"
        ]
    }

    /**
     * Met à jour les préférences d'alerte de l'utilisateur connecté
     * @param command Objet command contenant les données du formulaire
     * @return Redirige vers l'action index
     */
    def update(AlertPreferenceCommand command) {
        if (!command.validate()) {
            flash.error = "Erreur de validation des données"
            redirect(action: "index")
            return
        }
        
        def preference = alertPreferenceService.getPreference()
        if (!preference) {
            flash.error = "Préférence d'alerte non trouvée"
            redirect(action: "index")
            return
        }
        
        try {
            alertPreferenceService.updatePreference(preference.id, command.toPropertyMap())
            flash.message = "Préférences d'alerte mises à jour avec succès"
            redirect(action: "index")
        } catch (ValidationException e) {
            flash.error = "Erreur lors de la mise à jour des préférences d'alerte"
            redirect(action: "index")
        }
    }
}
