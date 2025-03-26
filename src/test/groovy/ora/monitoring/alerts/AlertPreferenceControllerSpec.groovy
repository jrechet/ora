package ora.monitoring.alerts

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import ora.auth.User
import spock.lang.Specification

class AlertPreferenceControllerSpec extends Specification implements ControllerUnitTest<AlertPreferenceController>, DataTest {

    def setupSpec() {
        mockDomains(AlertPreference, User)
    }
    
    def setup() {
        // Utilisation des capacités de Mock de Spock pour le service
        controller.alertPreferenceService = Mock(AlertPreferenceService)
    }

    def cleanup() {
    }

    void "test index action"() {
        given: "Une préférence d'alerte pour l'utilisateur connecté"
        def preference = new AlertPreference(emailEnabled: true, browserEnabled: false, systemEnabled: true)
        preference.id = 1L
        
        when: "La méthode index est appelée"
        1 * controller.alertPreferenceService.getPreference() >> preference
        def model = controller.index()
        
        then: "Le modèle contient la préférence et les bonnes valeurs"
        model.preference == preference
        model.pageTitle == "Préférences d'alerte"
        model.currentSection == "alertPreferences"
    }
    
    void "test index action quand aucune préférence n'existe"() {
        given: "Aucune préférence n'existe pour l'utilisateur"
        def newPreference = new AlertPreference(browserEnabled: true)
        newPreference.id = 1L
        
        when: "La méthode index est appelée"
        1 * controller.alertPreferenceService.getPreference() >> null
        1 * controller.alertPreferenceService.createDefaultPreference() >> newPreference
        def model = controller.index()
        
        then: "Le modèle contient la nouvelle préférence créée"
        model.preference == newPreference
        model.pageTitle == "Préférences d'alerte"
        model.currentSection == "alertPreferences"
    }
    
    void "test update action avec une préférence existante"() {
        given: "Une préférence existante"
        def preference = new AlertPreference(emailEnabled: false, browserEnabled: true)
        preference.id = 1L
        
        and: "Une préférence mise à jour"
        def updatedPreference = new AlertPreference(emailEnabled: true, browserEnabled: false)
        updatedPreference.id = 1L
        
        and: "Un objet command valide"
        def command = new AlertPreferenceCommand(
            emailEnabled: true,
            browserEnabled: false,
            systemEnabled: true,
            emailRecipients: "test@example.com"
        )
        
        and: "Configuration de la méthode HTTP POST"
        request.method = 'POST'
        
        when: "La méthode update est appelée"
        1 * controller.alertPreferenceService.getPreference() >> preference
        1 * controller.alertPreferenceService.updatePreference(1L, _) >> updatedPreference
        controller.update(command)
        
        then: "Une redirection est effectuée vers l'action index avec un message de succès"
        response.redirectedUrl == '/alertPreference/index'
        flash.message == "Préférences d'alerte mises à jour avec succès"
    }
    
    void "test update action avec un command invalide"() {
        given: "Un objet command invalide"
        def command = new AlertPreferenceCommand()
        command.emailEnabled = null // Violation de la contrainte nullable: false
        
        and: "Configuration de la méthode HTTP POST"
        request.method = 'POST'
        
        when: "La méthode update est appelée avec un command invalide"
        controller.update(command)
        
        then: "Une redirection est effectuée avec un message d'erreur"
        response.redirectedUrl == '/alertPreference/index'
        flash.error == "Erreur de validation des données"
    }
    
    void "test update action quand aucune préférence n'existe"() {
        given: "Un objet command valide"
        def command = new AlertPreferenceCommand(
            emailEnabled: true,
            browserEnabled: false,
            systemEnabled: true
        )
        
        and: "Configuration de la méthode HTTP POST"
        request.method = 'POST'
        
        when: "La méthode update est appelée mais aucune préférence n'existe"
        1 * controller.alertPreferenceService.getPreference() >> null
        controller.update(command)
        
        then: "Une redirection est effectuée avec un message d'erreur"
        response.redirectedUrl == '/alertPreference/index'
        flash.error == "Préférence d'alerte non trouvée"
    }
    
    void "test update action avec une erreur de validation"() {
        given: "Une préférence existante"
        def preference = new AlertPreference(emailEnabled: false, browserEnabled: true)
        preference.id = 1L
        
        and: "Un objet command valide"
        def command = new AlertPreferenceCommand(
            emailEnabled: true,
            browserEnabled: false,
            systemEnabled: true
        )
        
        and: "Configuration de la méthode HTTP POST"
        request.method = 'POST'
        
        when: "La méthode update est appelée mais une exception de validation est levée"
        1 * controller.alertPreferenceService.getPreference() >> preference
        1 * controller.alertPreferenceService.updatePreference(1L, _) >> { 
            throw new ValidationException("Erreur de validation", preference.errors) 
        }
        controller.update(command)
        
        then: "Une redirection est effectuée avec un message d'erreur"
        response.redirectedUrl == '/alertPreference/index'
        flash.error == "Erreur lors de la mise à jour des préférences d'alerte"
    }
}
