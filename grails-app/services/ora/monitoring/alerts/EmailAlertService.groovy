package ora.monitoring.alerts

import grails.config.Config
import grails.core.support.GrailsConfigurationAware

/**
 * Service pour envoyer des alertes par email.
 * Gère les notifications par email en cas de détection d'un problème.
 */
class EmailAlertService implements GrailsConfigurationAware {
    
    AlertPreferenceService alertPreferenceService
    
    /** Configuration email depuis application.yml */
    String mailFrom
    String mailHost
    Integer mailPort
    String mailUsername
    String mailPassword
    List<String> alertRecipients = []
    
    @Override
    void setConfiguration(Config co) {
        mailFrom = co.getProperty('grails.mail.from', String) ?: 'no-reply@ora-monitoring.com'
        mailHost = co.getProperty('grails.mail.host', String) ?: 'localhost'
        mailPort = co.getProperty('grails.mail.port', Integer) ?: 25
        mailUsername = co.getProperty('grails.mail.username', String) ?: ''
        mailPassword = co.getProperty('grails.mail.password', String) ?: ''
        
        def recipients = co.getProperty('ora.monitoring.alert.emailRecipients', String) ?: ''
        if (recipients) {
            alertRecipients = recipients.split(',').collect { it.trim() }
        }
    }
    
    /**
     * Méthode alternative pour configurer le service dans les tests
     */
    void configureForTests(Map config) {
        mailFrom = config.mailFrom ?: 'no-reply@ora-monitoring.com'
        mailHost = config.mailHost ?: 'localhost'
        mailPort = config.mailPort ?: 25
        mailUsername = config.mailUsername ?: ''
        mailPassword = config.mailPassword ?: ''
        
        def recipients = config.emailRecipients ?: ''
        if (recipients) {
            alertRecipients = recipients.split(',').collect { it.trim() }
        }
    }
    
    /**
     * Vérifie si les alertes par email sont activées.
     * @return true si les alertes email sont activées, false sinon
     */
    boolean isEnabled() {
        return alertPreferenceService.isAlertTypeEnabled('email')
    }
    
    /**
     * Envoie une alerte par email.
     * @param title Titre de l'alerte
     * @param message Contenu de l'alerte
     * @param severity Niveau de sévérité (info, warning, error, critical)
     * @return true si l'email a été envoyé, false sinon
     */
    boolean sendAlert(String title, String message, String severity = 'warning') {
        if (!isEnabled()) {
            log.debug("Alertes email désactivées, alerte non envoyée: ${title}")
            return false
        }
        
        if (alertRecipients.isEmpty()) {
            log.warn("Aucun destinataire configuré pour les alertes email")
            return false
        }
        
        try {
            // Simulation d'envoi d'email pour le moment
            // Dans une implémentation réelle, on utiliserait le plugin mail de Grails
            log.info("Envoi d'alerte email - Titre: ${title}, Sévérité: ${severity}")
            log.debug("Destinataires: ${alertRecipients.join(', ')}")
            log.debug("Message: ${message}")
            
            // TODO: Implémenter l'envoi réel avec le plugin mail de Grails
            // sendMail {
            //    to alertRecipients
            //    from mailFrom
            //    subject "[ORA ${severity.toUpperCase()}] ${title}"
            //    html message
            // }
            
            return true
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'alerte email: ${e.message}", e)
            return false
        }
    }
}
