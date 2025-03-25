package ora.monitoring.alerts

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import groovy.json.JsonOutput

/**
 * Service pour envoyer des alertes au navigateur.
 * Utilise WebSockets pour envoyer des notifications aux navigateurs connectés.
 */
class BrowserAlertService implements GrailsConfigurationAware {
    
    AlertPreferenceService alertPreferenceService
    def grailsApplication
    
    /** Configuration pour les alertes navigateur depuis application.yml */
    boolean enableNotifications = true
    int notificationTimeout = 10000  // en millisecondes
    
    @Override
    void setConfiguration(Config co) {
        enableNotifications = co.getProperty('ora.monitoring.alert.browser.enabled', Boolean) ?: true
        notificationTimeout = co.getProperty('ora.monitoring.alert.browser.timeout', Integer) ?: 10000
    }
    
    /**
     * Méthode alternative pour configurer le service dans les tests
     */
    void configureForTests(Map config) {
        enableNotifications = config.enabled != null ? config.enabled : true
        notificationTimeout = config.timeout ?: 10000
    }
    
    /**
     * Vérifie si les alertes navigateur sont activées.
     * @return true si les alertes navigateur sont activées, false sinon
     */
    boolean isEnabled() {
        return enableNotifications && alertPreferenceService.isAlertTypeEnabled('browser')
    }
    
    /**
     * Envoie une alerte aux navigateurs connectés.
     * @param title Titre de l'alerte
     * @param message Contenu de l'alerte
     * @param severity Niveau de sévérité (info, warning, error, critical)
     * @return true si l'alerte a été envoyée, false sinon
     */
    boolean sendAlert(String title, String message, String severity = 'warning') {
        if (!isEnabled()) {
            log.debug("Alertes navigateur désactivées, alerte non envoyée: ${title}")
            return false
        }
        
        try {
            // Construction de l'objet d'alerte
            def alert = [
                type: 'browser-alert',
                title: title,
                message: message,
                severity: severity,
                timestamp: System.currentTimeMillis(),
                timeout: notificationTimeout
            ]
            
            // Conversion en JSON
            String alertJson = JsonOutput.toJson(alert)
            
            // Simulation d'envoi via WebSocket pour le moment
            log.info("Envoi d'alerte navigateur - Titre: ${title}, Sévérité: ${severity}")
            log.debug("Message JSON: ${alertJson}")
            
            // TODO: Implémenter l'envoi réel via WebSocket
            // Cela pourrait ressembler à :
            // WebSocketService.broadcastMessage(alertJson)
            
            return true
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'alerte navigateur: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Obtient le statut actuel de la configuration des alertes navigateur.
     * @return Map avec les informations de statut
     */
    Map getStatus() {
        return [
            enabled: isEnabled(),
            globalSetting: enableNotifications,
            userPreference: alertPreferenceService.isAlertTypeEnabled('browser'),
            timeout: notificationTimeout
        ]
    }
}
