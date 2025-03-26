package ora.monitoring.alerts

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import groovy.json.JsonOutput
import java.util.UUID

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
            
            // Création d'une notification pour l'interface web
            def notification = [
                id: UUID.randomUUID().toString(),
                title: title,
                message: message,
                severity: severity,
                timestamp: new Date()
            ]
            
            // Stockage de la notification pour récupération ultérieure
            storeNotification(notification)
            
            // Diffusion de la notification via WebSocket
            broadcastNotification(notification)
            
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
     * Stocke une notification pour récupération ultérieure.
     * @param notification La notification à stocker
     */
    private void storeNotification(Map notification) {
        // Dans une implémentation réelle, on stockerait la notification en base de données
        // ou dans un cache distribué pour permettre sa récupération par les clients web
        log.debug("Stockage de la notification: ${notification.id}")
    }
    
    /**
     * Diffuse une notification via WebSocket.
     * @param notification La notification à diffuser
     */
    private void broadcastNotification(Map notification) {
        // Dans une implémentation réelle, on utiliserait un mécanisme de WebSocket
        // pour diffuser la notification à tous les clients connectés
        log.debug("Diffusion de la notification: ${notification.id}")
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
