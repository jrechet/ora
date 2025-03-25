package ora.monitoring.alerts

import grails.config.Config
import grails.core.support.GrailsConfigurationAware

/**
 * Service pour envoyer des alertes système (notifications du système d'exploitation).
 * Gère les notifications système en cas de détection d'un problème.
 */
class SystemAlertService implements GrailsConfigurationAware {
    
    AlertPreferenceService alertPreferenceService
    
    /** Configuration pour les alertes système depuis application.yml */
    boolean enableNotifications = true
    String notificationCommand = null
    
    @Override
    void setConfiguration(Config co) {
        enableNotifications = co.getProperty('ora.monitoring.alert.system.enabled', Boolean) ?: true
        notificationCommand = co.getProperty('ora.monitoring.alert.system.command', String) ?: null
    }
    
    /**
     * Méthode alternative pour configurer le service dans les tests
     */
    void configureForTests(Map config) {
        enableNotifications = config.enabled != null ? config.enabled : true
        notificationCommand = config.command
    }
    
    /**
     * Vérifie si les alertes système sont activées.
     * @return true si les alertes système sont activées, false sinon
     */
    boolean isEnabled() {
        return enableNotifications && alertPreferenceService.isAlertTypeEnabled('system')
    }
    
    /**
     * Envoie une alerte système.
     * @param title Titre de l'alerte
     * @param message Contenu de l'alerte
     * @param severity Niveau de sévérité (info, warning, error, critical)
     * @return true si l'alerte a été envoyée, false sinon
     */
    boolean sendAlert(String title, String message, String severity = 'warning') {
        if (!isEnabled()) {
            log.debug("Alertes système désactivées, alerte non envoyée: ${title}")
            return false
        }
        
        try {
            log.info("Envoi d'alerte système - Titre: ${title}, Sévérité: ${severity}")
            
            // Détection du système d'exploitation
            String os = System.getProperty('os.name').toLowerCase()
            
            if (notificationCommand) {
                // Utiliser la commande de notification personnalisée
                executeNotificationCommand(notificationCommand, title, message, severity)
            } else if (os.contains('mac')) {
                // macOS - Utiliser osascript pour afficher une notification
                sendMacOSNotification(title, message, severity)
            } else if (os.contains('win')) {
                // Windows - Utiliser PowerShell pour afficher une notification
                sendWindowsNotification(title, message, severity)
            } else if (os.contains('linux') || os.contains('unix')) {
                // Linux - Utiliser notify-send pour afficher une notification
                sendLinuxNotification(title, message, severity)
            } else {
                log.warn("Système d'exploitation non pris en charge pour les alertes système: ${os}")
                return false
            }
            
            return true
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'alerte système: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Exécute la commande de notification personnalisée.
     */
    private boolean executeNotificationCommand(String command, String title, String message, String severity) {
        if (!command) return false
        
        // Remplacer les variables dans la commande
        String cmd = command
            .replace('${title}', title.replaceAll('"', '\\\\"'))
            .replace('${message}', message.replaceAll('"', '\\\\"'))
            .replace('${severity}', severity)
        
        log.debug("Exécution de la commande de notification: ${cmd}")
        
        // Exécution de la commande
        def process = cmd.execute()
        process.waitFor()
        
        if (process.exitValue() != 0) {
            log.warn("Échec de la commande de notification: ${process.err.text}")
            return false
        }
        
        return true
    }
    
    /**
     * Envoie une notification sur macOS.
     */
    private boolean sendMacOSNotification(String title, String message, String severity) {
        String script = """
            display notification "${message.replaceAll('"', '\\\\"')}" with title "${title.replaceAll('"', '\\\\"')}" subtitle "ORA ${severity.toUpperCase()}"
        """
        
        // Exécution du script AppleScript
        def process = ["osascript", "-e", script].execute()
        process.waitFor()
        
        if (process.exitValue() != 0) {
            log.warn("Échec de la notification macOS: ${process.err.text}")
            return false
        }
        
        return true
    }
    
    /**
     * Envoie une notification sur Windows.
     */
    private boolean sendWindowsNotification(String title, String message, String severity) {
        // Script PowerShell pour afficher une notification
        String script = """
            [Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType = WindowsRuntime] | Out-Null
            [Windows.Data.Xml.Dom.XmlDocument, Windows.Data.Xml.Dom.XmlDocument, ContentType = WindowsRuntime] | Out-Null
            
            \$app = 'ORA.Monitoring'
            \$template = @"
            <toast>
                <visual>
                    <binding template="ToastText02">
                        <text id="1">$title</text>
                        <text id="2">$message</text>
                    </binding>
                </visual>
            </toast>
            "@
            
            \$xml = New-Object Windows.Data.Xml.Dom.XmlDocument
            \$xml.LoadXml(\$template)
            \$toast = [Windows.UI.Notifications.ToastNotification]::new(\$xml)
            [Windows.UI.Notifications.ToastNotificationManager]::CreateToastNotifier(\$app).Show(\$toast)
        """
        
        // Exécution du script PowerShell
        def process = ["powershell", "-Command", script].execute()
        process.waitFor()
        
        if (process.exitValue() != 0) {
            log.warn("Échec de la notification Windows: ${process.err.text}")
            return false
        }
        
        return true
    }
    
    /**
     * Envoie une notification sur Linux.
     */
    private boolean sendLinuxNotification(String title, String message, String severity) {
        // Utilisation de notify-send pour afficher une notification
        String urgency = severity == 'critical' ? 'critical' : (severity == 'error' ? 'critical' : (severity == 'warning' ? 'normal' : 'low'))
        def process = ["notify-send", "-u", urgency, "ORA ${severity.toUpperCase()}: ${title}", message].execute()
        process.waitFor()
        
        if (process.exitValue() != 0) {
            log.warn("Échec de la notification Linux: ${process.err.text}")
            return false
        }
        
        return true
    }
}
