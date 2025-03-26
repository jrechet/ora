package ora.monitoring.alerts

import grails.validation.Validateable

/**
 * Objet command pour la gestion des préférences d'alerte.
 * Utilisé pour récupérer les données du formulaire de manière structurée
 * et éviter de passer les params directement à la couche service.
 */
class AlertPreferenceCommand implements Validateable {

    /** Active/désactive les notifications par email */
    Boolean emailEnabled = false

    /** Active/désactive les notifications dans le navigateur */
    Boolean browserEnabled = false

    /** Active/désactive les notifications système (windows, macos) */
    Boolean systemEnabled = false

    /** Liste des destinataires email pour cet utilisateur */
    String emailRecipients

    /** ID de la préférence à mettre à jour */
    Long id

    static constraints = {
        emailEnabled nullable: false
        browserEnabled nullable: false
        systemEnabled nullable: false
        emailRecipients nullable: true, blank: true
        id nullable: true
    }

    /**
     * Convertit cet objet command en une map de propriétés
     * pouvant être utilisée pour mettre à jour une instance d'AlertPreference
     * @return Map des propriétés à mettre à jour
     */
    Map toPropertyMap() {
        return [
            emailEnabled: emailEnabled,
            browserEnabled: browserEnabled,
            systemEnabled: systemEnabled,
            emailRecipients: emailRecipients
        ]
    }
}
