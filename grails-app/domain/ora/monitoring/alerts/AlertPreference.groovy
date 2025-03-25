package ora.monitoring.alerts

/**
 * Représente les préférences d'alerte dans l'application.
 * Ces préférences définissent comment l'utilisateur souhaite être notifié
 * en cas de détection d'un problème.
 */
class AlertPreference {

    /** Active/désactive les notifications par email */
    boolean emailEnabled = false
    
    /** Active/désactive les notifications dans le navigateur */
    boolean browserEnabled = true
    
    /** Active/désactive les notifications système (windows, macos) */
    boolean systemEnabled = false
    
    /** Indique si cette configuration est la configuration active */
    boolean active = true
    
    /** Nom de la configuration (pour permettre plusieurs configurations) */
    String name = "Default"
    
    /** Date de création */
    Date dateCreated
    
    /** Date de dernière modification */
    Date lastUpdated

    static constraints = {
        name blank: false, nullable: false, unique: true
        dateCreated nullable: true
        lastUpdated nullable: true
    }
    
    static mapping = {
        table 'alert_preferences'
        version true
    }
    
    /**
     * Méthode utilitaire pour récupérer la configuration active.
     * @return La configuration active ou une nouvelle configuration par défaut si aucune n'existe
     */
    static AlertPreference getActivePreference() {
        def preference = AlertPreference.findByActive(true)
        if (!preference) {
            preference = new AlertPreference(active: true)
            preference.save(flush: true)
        }
        return preference
    }
}
