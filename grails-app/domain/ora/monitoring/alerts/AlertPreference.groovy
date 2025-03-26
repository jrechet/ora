package ora.monitoring.alerts

import ora.auth.User

import java.time.LocalDateTime

/**
 * Représente les préférences d'alerte dans l'application.
 * Ces préférences définissent comment l'utilisateur souhaite être notifié
 * en cas de détection d'un problème.
 */
class AlertPreference {

    /** Active/désactive les notifications par email */
    boolean emailEnabled = false

    /** Active/désactive les notifications dans le navigateur */
    boolean browserEnabled = false

    /** Active/désactive les notifications système (windows, macos) */
    boolean systemEnabled = false

    /** Liste des destinataires email pour cet utilisateur */
    String emailRecipients

    /** Date de création */
    LocalDateTime dateCreated

    /** Date de dernière modification */
    LocalDateTime lastUpdated

    static belongsTo = [user: User]

    static constraints = {
        user nullable: false, unique: true
        emailRecipients nullable: true, blank: true
        dateCreated nullable: true
        lastUpdated nullable: true
    }

    static mapping = {
        table 'alert_preferences'
        version true
    }
}
