package ora.monitoring.alerts

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import ora.auth.User
import spock.lang.Specification

class BrowserAlertServiceSpec extends Specification implements ServiceUnitTest<BrowserAlertService>, DataTest {

    def setupSpec() {
        mockDomains(AlertPreference, User)
    }
    
    User testUser

    def setup() {
        // Créer un utilisateur de test
        testUser = new User(username: 'testuser', password: 'password').save(flush: true)
        
        // Mock de AlertPreferenceService
        service.alertPreferenceService = Mock(AlertPreferenceService)
        
        // Configuration de test
        service.configureForTests([
            enabled: true,
            timeout: 5000
        ])
    }

    void "test isEnabled returns true when global setting and user preference are enabled"() {
        given:
        service.configureForTests([enabled: true])
        service.alertPreferenceService.isAlertTypeEnabled('browser') >> true
        
        expect:
        service.isEnabled()
    }
    
    void "test isEnabled returns false when global setting is disabled"() {
        given:
        service.configureForTests([enabled: false])
        service.alertPreferenceService.isAlertTypeEnabled('browser') >> true
        
        expect:
        !service.isEnabled()
    }
    
    void "test isEnabled returns false when user preference is disabled"() {
        given:
        service.configureForTests([enabled: true])
        service.alertPreferenceService.isAlertTypeEnabled('browser') >> false
        
        expect:
        !service.isEnabled()
    }
    
    void "test sendAlert returns false when browser alerts are disabled"() {
        given:
        service.configureForTests([enabled: false])
        
        when:
        def result = service.sendAlert("Test Alert", "This is a test alert")
        
        then:
        !result
    }
    
    void "test sendAlert returns true when browser alerts are enabled"() {
        given:
        service.configureForTests([enabled: true])
        service.alertPreferenceService.isAlertTypeEnabled('browser') >> true
        
        when:
        def result = service.sendAlert("Test Alert", "This is a test alert")
        
        then:
        result
    }
    
    void "test sendAlert with different severity levels"() {
        given:
        service.configureForTests([enabled: true])
        service.alertPreferenceService.isAlertTypeEnabled('browser') >> true
        
        expect:
        service.sendAlert("Info Alert", "This is an info alert", "info")
        service.sendAlert("Warning Alert", "This is a warning alert", "warning")
        service.sendAlert("Error Alert", "This is an error alert", "error")
        service.sendAlert("Critical Alert", "This is a critical alert", "critical")
    }
    
    void "test getStatus returns correct status information"() {
        given:
        service.configureForTests([
            enabled: true,
            timeout: 8000
        ])
        service.alertPreferenceService.isAlertTypeEnabled('browser') >> true
        
        when:
        def status = service.getStatus()
        
        then:
        status.enabled
        status.globalSetting
        status.userPreference
        status.timeout == 8000
    }
    
    void "test configuration is loaded correctly"() {
        given:
        service.configureForTests([
            enabled: false,
            timeout: 15000
        ])
        
        expect:
        !service.enableNotifications
        service.notificationTimeout == 15000
    }
    
    void "test default values are used when configuration is missing"() {
        given:
        service.configureForTests([:])
        
        expect:
        service.enableNotifications
        service.notificationTimeout == 10000
    }
}
