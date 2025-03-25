package ora.monitoring.alerts

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class AlertPreferenceServiceSpec extends Specification implements ServiceUnitTest<AlertPreferenceService>, DataTest {

    def setupSpec() {
        mockDomain AlertPreference
    }

    def setup() {
        // Créer des préférences de test
        new AlertPreference(
            name: "Test Preference 1",
            emailEnabled: true,
            browserEnabled: false,
            systemEnabled: true,
            active: true
        ).save(flush: true)
        
        new AlertPreference(
            name: "Test Preference 2",
            emailEnabled: false,
            browserEnabled: true,
            systemEnabled: false,
            active: false
        ).save(flush: true)
    }

    def cleanup() {
        AlertPreference.list()*.delete(flush: true)
    }
    
    void "test getActivePreference returns the active preference"() {
        when:
        def preference = service.getActivePreference()
        
        then:
        preference != null
        preference.name == "Test Preference 1"
        preference.emailEnabled
        !preference.browserEnabled
        preference.systemEnabled
    }
    
    void "test getAllPreferences returns all preferences"() {
        when:
        def preferences = service.getAllPreferences()
        
        then:
        preferences.size() == 2
        preferences.find { it.name == "Test Preference 1" } != null
        preferences.find { it.name == "Test Preference 2" } != null
    }
    
    void "test getPreferenceById returns specific preference"() {
        given:
        def pref1 = AlertPreference.findByName("Test Preference 1")
        
        when:
        def result = service.getPreferenceById(pref1.id)
        
        then:
        result != null
        result.name == "Test Preference 1"
        result.id == pref1.id
        
        when: "requesting a non-existent ID"
        result = service.getPreferenceById(999)
        
        then: "result should be null"
        result == null
    }
    
    void "test savePreference correctly saves a new preference"() {
        given:
        def newPref = new AlertPreference(
            name: "New Preference",
            emailEnabled: true,
            browserEnabled: true,
            systemEnabled: true
        )
        
        when:
        def result = service.savePreference(newPref)
        
        then:
        result != null
        result.id != null
        AlertPreference.count() == 3
        AlertPreference.findByName("New Preference") != null
    }
    
    void "test updatePreference correctly updates an existing preference"() {
        given:
        def pref = AlertPreference.findByName("Test Preference 1")
        def params = [
            emailEnabled: false,
            browserEnabled: true,
            systemEnabled: false
        ]
        
        when:
        def result = service.updatePreference(pref.id, params)
        
        then:
        result != null
        !result.emailEnabled
        result.browserEnabled
        !result.systemEnabled
        result.name == "Test Preference 1"  // Nom inchangé
        
        when: "updating a non-existent preference"
        result = service.updatePreference(999, params)
        
        then: "result should be null"
        result == null
    }
    
    void "test setActivePreference correctly activates a preference and deactivates others"() {
        given:
        def pref2 = AlertPreference.findByName("Test Preference 2")
        
        when:
        def result = service.setActivePreference(pref2.id)
        
        then:
        result != null
        result.active
        result.name == "Test Preference 2"
        
        and: "the previously active preference should be deactivated"
        def pref1 = AlertPreference.findByName("Test Preference 1")
        !pref1.active
        
        when: "activating a non-existent preference"
        result = service.setActivePreference(999)
        
        then: "result should be null"
        result == null
    }
    
    void "test deletePreference correctly deletes a preference"() {
        given:
        def pref2 = AlertPreference.findByName("Test Preference 2")
        
        when:
        def result = service.deletePreference(pref2.id)
        
        then:
        result
        AlertPreference.count() == 1
        AlertPreference.findByName("Test Preference 2") == null
        
        when: "deleting the active preference"
        def pref1 = AlertPreference.findByName("Test Preference 1")
        result = service.deletePreference(pref1.id)
        
        then: "a new default preference should be created"
        result
        AlertPreference.count() == 1
        AlertPreference.findByActive(true) != null
        
        when: "deleting a non-existent preference"
        result = service.deletePreference(999)
        
        then: "result should be false"
        !result
    }
    
    void "test isAlertTypeEnabled correctly checks if a type is enabled"() {
        expect: "email alerts should be enabled"
        service.isAlertTypeEnabled('email')
        
        and: "browser alerts should be disabled"
        !service.isAlertTypeEnabled('browser')
        
        and: "system alerts should be enabled"
        service.isAlertTypeEnabled('system')
        
        and: "unknown alert types should be disabled"
        !service.isAlertTypeEnabled('unknown')
    }
}
