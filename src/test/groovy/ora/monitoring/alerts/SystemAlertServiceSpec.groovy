package ora.monitoring.alerts

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class SystemAlertServiceSpec extends Specification implements ServiceUnitTest<SystemAlertService>, DataTest {

    def setupSpec() {
        mockDomain AlertPreference
    }

    def setup() {
        // Mock de AlertPreferenceService
        service.alertPreferenceService = Mock(AlertPreferenceService)
        
        // Configuration de test
        service.configureForTests([
            enabled: true,
            command: null
        ])
        
        // Mocker les méthodes d'envoi de notification pour éviter les appels système réels
        service.metaClass.executeNotificationCommand = { String cmd, String title, String message, String severity -> true }
        service.metaClass.sendMacOSNotification = { String title, String message, String severity -> true }
        service.metaClass.sendWindowsNotification = { String title, String message, String severity -> true }
        service.metaClass.sendLinuxNotification = { String title, String message, String severity -> true }
    }

    void "test isEnabled returns true when global setting and user preference are enabled"() {
        given:
        service.configureForTests([enabled: true])
        service.alertPreferenceService.isAlertTypeEnabled('system') >> true
        
        expect:
        service.isEnabled()
    }
    
    void "test isEnabled returns false when global setting is disabled"() {
        given:
        service.configureForTests([enabled: false])
        service.alertPreferenceService.isAlertTypeEnabled('system') >> true
        
        expect:
        !service.isEnabled()
    }
    
    void "test isEnabled returns false when user preference is disabled"() {
        given:
        service.configureForTests([enabled: true])
        service.alertPreferenceService.isAlertTypeEnabled('system') >> false
        
        expect:
        !service.isEnabled()
    }
    
    void "test sendAlert returns false when system alerts are disabled"() {
        given:
        service.configureForTests([enabled: false])
        
        when:
        def result = service.sendAlert("Test Alert", "This is a test alert")
        
        then:
        !result
    }
    
    void "test sendAlert uses custom notification command when configured"() {
        given:
        service.configureForTests([
            enabled: true,
            command: 'echo "${title}: ${message} (${severity})" > /dev/null'
        ])
        service.alertPreferenceService.isAlertTypeEnabled('system') >> true
        
        when:
        def result = service.sendAlert("Test Alert", "This is a test alert", "warning")
        
        then:
        result
    }
    
    void "test sendAlert handles macOS notifications"() {
        given:
        service.configureForTests([enabled: true])
        service.alertPreferenceService.isAlertTypeEnabled('system') >> true
        
        and: "Mocker le système d'exploitation macOS"
        def originalProperty = System.getProperty('os.name')
        System.setProperty('os.name', 'Mac OS X')
        
        when:
        def result = service.sendAlert("Test Alert", "This is a test alert")
        
        then:
        result
        
        cleanup:
        System.setProperty('os.name', originalProperty ?: "")
    }
    
    void "test sendAlert handles Windows notifications"() {
        given:
        service.configureForTests([enabled: true])
        service.alertPreferenceService.isAlertTypeEnabled('system') >> true
        
        and: "Mocker le système d'exploitation Windows"
        def originalProperty = System.getProperty('os.name')
        System.setProperty('os.name', 'Windows 10')
        
        and: "S'assurer que la méthode mock est bien définie"
        service.metaClass.sendWindowsNotification = { String title, String message, String severity -> true }
        
        when:
        def result = service.sendAlert("Test Alert", "This is a test alert")
        
        then:
        result
        
        cleanup:
        System.setProperty('os.name', originalProperty ?: "")
    }
    
    void "test sendAlert handles Linux notifications"() {
        given:
        service.configureForTests([enabled: true])
        service.alertPreferenceService.isAlertTypeEnabled('system') >> true
        
        and: "Mocker le système d'exploitation Linux"
        def originalProperty = System.getProperty('os.name')
        System.setProperty('os.name', 'Linux')
        
        and: "S'assurer que la méthode mock est bien définie"
        service.metaClass.sendLinuxNotification = { String title, String message, String severity -> true }
        
        when:
        def result = service.sendAlert("Test Alert", "This is a test alert")
        
        then:
        result
        
        cleanup:
        System.setProperty('os.name', originalProperty ?: "")
    }
    
    void "test sendAlert handles unknown operating systems"() {
        given:
        service.configureForTests([enabled: true])
        service.alertPreferenceService.isAlertTypeEnabled('system') >> true
        
        and: "Mocker un système d'exploitation inconnu"
        def originalProperty = System.getProperty('os.name')
        System.setProperty('os.name', 'SomeUnknownOS')
        
        when:
        def result = service.sendAlert("Test Alert", "This is a test alert")
        
        then:
        !result
        
        cleanup:
        System.setProperty('os.name', originalProperty ?: "")
    }
    
    void "test configuration is loaded correctly"() {
        given:
        service.configureForTests([
            enabled: false,
            command: 'custom-notify "${title}" "${message}"'
        ])
        
        expect:
        !service.enableNotifications
        service.notificationCommand == 'custom-notify "${title}" "${message}"'
    }
    
    void "test default values are used when configuration is missing"() {
        given:
        service.configureForTests([:])
        
        expect:
        service.enableNotifications
        service.notificationCommand == null
    }
}
