package ora.monitoring.alerts

import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import ora.auth.User

/**
 * Service pour gérer les préférences d'alerte.
 * Fournit des méthodes pour récupérer, sauvegarder et gérer les préférences
 * d'alerte dans l'application.
 */
@Transactional
class AlertPreferenceService {

    SpringSecurityService springSecurityService

    /**
     * Récupère l'utilisateur actuellement connecté
     * @return L'utilisateur connecté ou null si aucun utilisateur n'est connecté
     */
    User getCurrentUser() {
        return springSecurityService.currentUser as User
    }

    /**
     * Récupère la préférence d'alerte pour l'utilisateur connecté.
     * @return La préférence ou null si aucune n'existe
     */
    @Transactional(readOnly = true)
    AlertPreference getPreference() {
        User currentUser = getCurrentUser()
        if (!currentUser) {
            return null
        }
        return AlertPreference.findByUser(currentUser)
    }

    /**
     * Récupère la préférence d'alerte pour un utilisateur spécifique.
     * @param user L'utilisateur pour lequel récupérer les préférences
     * @return La préférence ou null si aucune n'existe
     */
    @Transactional(readOnly = true)
    AlertPreference getPreferenceForUser(User user) {
        if (!user) {
            return null
        }
        return AlertPreference.findByUser(user)
    }

    /**
     * Crée une préférence par défaut pour l'utilisateur connecté si elle n'existe pas
     * @return La préférence créée ou existante
     */
    @Transactional
    AlertPreference createDefaultPreference() {
        User currentUser = getCurrentUser()
        if (!currentUser) {
            return null
        }

        AlertPreference preference = getPreferenceForUser(currentUser)
        if (!preference) {
            preference = new AlertPreference(
                    user: currentUser,
            )
            if (!preference.save(flush: true)) {
                log.error("Erreur lors de la création de la préférence d'alerte: ${preference.errors}")
                return null
            }
        }
        return preference
    }

    /**
     * Récupère une préférence d'alerte par son ID.
     * @param id L'ID de la préférence d'alerte
     * @return La préférence trouvée ou null si non trouvée
     */
    @Transactional(readOnly = true)
    AlertPreference getPreferenceById(Long id) {
        return AlertPreference.get(id)
    }


    /**
     * Met à jour une préférence d'alerte existante.
     * @param id L'ID de la préférence à mettre à jour
     * @param params Les nouveaux paramètres (emailEnabled, browserEnabled, systemEnabled)
     * @return La préférence mise à jour ou null en cas d'erreur
     */
    @Transactional
    AlertPreference updatePreference(Long id, Map params) {
        AlertPreference preference = AlertPreference.get(id)
        if (!preference) {
            log.error("Préférence d'alerte non trouvée avec l'ID: ${id}")
            return null
        }

        preference.properties = params
        preference.save(failOnError: true)
    }

    /**
     * Vérifie si un type d'alerte spécifique est activé pour l'utilisateur connecté.
     * @param type Le type d'alerte ('email', 'browser', 'system')
     * @return true si ce type d'alerte est activé, false sinon
     */
    @Transactional(readOnly = true)
    boolean isAlertTypeEnabled(String type) {
        AlertPreference preference = getPreference()
        if (!preference) {
            return false
        }

        switch (type.toLowerCase()) {
            case 'email':
                return preference.emailEnabled
            case 'browser':
                return preference.browserEnabled
            case 'system':
                return preference.systemEnabled
            default:
                log.warn("Type d'alerte inconnu: ${type}")
                return false
        }
    }

    /**
     * Récupère les destinataires email configurés pour l'utilisateur connecté.
     * @return Liste des adresses email des destinataires
     */
    @Transactional(readOnly = true)
    List<String> getEmailRecipients() {
        AlertPreference preference = getPreference()
        if (!preference || !preference.emailRecipients) {
            return []
        }

        return preference.emailRecipients.split(',').collect { it.trim() }
    }
}
