package ora.monitoring.alerts

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class EmailAlertServiceSpec extends Specification implements ServiceUnitTest<EmailAlertService>, DataTest {

    def setupSpec() {
        mockDomain AlertPreference
    }

    def setup() {
        // Mock de AlertPreferenceService
        service.alertPreferenceService = Mock(AlertPreferenceService)
        
        // Configuration de test
        service.configureForTests([
            mailFrom: "test@example.com",
            mailHost: "smtp.example.com",
            mailPort: 587,
            emailRecipients: "admin@example.com, support@example.com"
        ])
    }

    void "test isEnabled returns true when email alerts are enabled"() {
        given:
        service.alertPreferenceService.isAlertTypeEnabled('email') >> true
        
        expect:
        service.isEnabled()
    }
    
    void "test isEnabled returns false when email alerts are disabled"() {
        given:
        service.alertPreferenceService.isAlertTypeEnabled('email') >> false
        
        expect:
        !service.isEnabled()
    }
    
    void "test sendAlert returns false when email alerts are disabled"() {
        given:
        service.alertPreferenceService.isAlertTypeEnabled('email') >> false
        
        when:
        def result = service.sendAlert("Test Alert", "This is a test alert")
        
        then:
        !result
    }
    
    void "test sendAlert returns false when no recipients are configured"() {
        given:
        service.alertPreferenceService.isAlertTypeEnabled('email') >> true
        service.configureForTests([
            mailFrom: "test@example.com",
            mailHost: "smtp.example.com",
            mailPort: 587,
            emailRecipients: ""
        ])
        
        when:
        def result = service.sendAlert("Test Alert", "This is a test alert")
        
        then:
        !result
    }
    
    void "test sendAlert returns true when email alerts are enabled and recipients are configured"() {
        given:
        service.alertPreferenceService.isAlertTypeEnabled('email') >> true
        
        when:
        def result = service.sendAlert("Test Alert", "This is a test alert")
        
        then:
        result
    }
    
    void "test sendAlert with different severity levels"() {
        given:
        service.alertPreferenceService.isAlertTypeEnabled('email') >> true
        
        expect:
        service.sendAlert("Info Alert", "This is an info alert", "info")
        service.sendAlert("Warning Alert", "This is a warning alert", "warning")
        service.sendAlert("Error Alert", "This is an error alert", "error")
        service.sendAlert("Critical Alert", "This is a critical alert", "critical")
    }
    
    void "test configuration is loaded correctly"() {
        given:
        service.configureForTests([
            mailFrom: 'custom@example.com',
            mailHost: 'custom-smtp.example.com',
            mailPort: 465,
            mailUsername: 'user',
            mailPassword: 'pass',
            emailRecipients: 'user1@example.com, user2@example.com'
        ])
        
        expect:
        service.mailFrom == 'custom@example.com'
        service.mailHost == 'custom-smtp.example.com'
        service.mailPort == 465
        service.mailUsername == 'user'
        service.mailPassword == 'pass'
        service.alertRecipients == ['user1@example.com', 'user2@example.com']
    }
    
    void "test default values are used when configuration is missing"() {
        given:
        service.configureForTests([:])
        
        expect:
        service.mailFrom == 'no-reply@ora-monitoring.com'
        service.mailHost == 'localhost'
        service.mailPort == 25
        service.mailUsername == ''
        service.mailPassword == ''
        service.alertRecipients.isEmpty()
    }
}
