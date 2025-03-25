package ora.monitoring.alerts

import grails.gorm.transactions.Transactional

/**
 * Service pour gérer les préférences d'alerte.
 * Fournit des méthodes pour récupérer, sauvegarder et gérer les préférences
 * d'alerte dans l'application.
 */
@Transactional
class AlertPreferenceService {

    /**
     * Récupère la préférence d'alerte active.
     * @return La préférence active ou null si aucune n'existe
     */
    AlertPreference getActivePreference() {
        return AlertPreference.getActivePreference()
    }
    
    /**
     * Récupère toutes les préférences d'alerte.
     * @return Liste de toutes les préférences d'alerte
     */
    List<AlertPreference> getAllPreferences() {
        return AlertPreference.list()
    }
    
    /**
     * Récupère une préférence d'alerte par son ID.
     * @param id L'ID de la préférence d'alerte
     * @return La préférence trouvée ou null si non trouvée
     */
    AlertPreference getPreferenceById(Long id) {
        return AlertPreference.get(id)
    }
    
    /**
     * Sauvegarde une préférence d'alerte.
     * @param preference La préférence à sauvegarder
     * @return La préférence sauvegardée ou null en cas d'erreur
     */
    AlertPreference savePreference(AlertPreference preference) {
        if (!preference.save(flush: true)) {
            log.error("Erreur lors de la sauvegarde de la préférence d'alerte: ${preference.errors}")
            return null
        }
        return preference
    }
    
    /**
     * Met à jour une préférence d'alerte existante.
     * @param id L'ID de la préférence à mettre à jour
     * @param params Les nouveaux paramètres (emailEnabled, browserEnabled, systemEnabled)
     * @return La préférence mise à jour ou null en cas d'erreur
     */
    AlertPreference updatePreference(Long id, Map params) {
        AlertPreference preference = AlertPreference.get(id)
        if (!preference) {
            log.error("Préférence d'alerte non trouvée avec l'ID: ${id}")
            return null
        }
        
        preference.properties = params
        return savePreference(preference)
    }
    
    /**
     * Définit une préférence comme active et désactive toutes les autres.
     * @param id L'ID de la préférence à activer
     * @return La préférence activée ou null en cas d'erreur
     */
    @Transactional
    AlertPreference setActivePreference(Long id) {
        AlertPreference preference = AlertPreference.get(id)
        if (!preference) {
            log.error("Préférence d'alerte non trouvée avec l'ID: ${id}")
            return null
        }
        
        // Désactiver toutes les préférences actives
        AlertPreference.findAllByActive(true).each { pref ->
            if (pref.id != id) {
                pref.active = false
                pref.save(flush: true)
            }
        }
        
        // Activer la préférence demandée
        preference.active = true
        return savePreference(preference)
    }
    
    /**
     * Supprime une préférence d'alerte.
     * @param id L'ID de la préférence à supprimer
     * @return true si suppression réussie, false sinon
     */
    boolean deletePreference(Long id) {
        AlertPreference preference = AlertPreference.get(id)
        if (!preference) {
            log.error("Préférence d'alerte non trouvée avec l'ID: ${id}")
            return false
        }
        
        // Si on supprime la préférence active, on en crée une nouvelle par défaut
        boolean wasActive = preference.active
        try {
            preference.delete(flush: true)
            if (wasActive && AlertPreference.count() > 0) {
                // Activer une autre préférence existante
                setActivePreference(AlertPreference.list().first().id)
            } else if (wasActive) {
                // Créer une nouvelle préférence active par défaut
                savePreference(new AlertPreference(active: true))
            }
            return true
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la préférence d'alerte: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Vérifie si un type d'alerte spécifique est activé.
     * @param type Le type d'alerte ('email', 'browser', 'system')
     * @return true si ce type d'alerte est activé, false sinon
     */
    boolean isAlertTypeEnabled(String type) {
        AlertPreference preference = getActivePreference()
        switch(type.toLowerCase()) {
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
}
