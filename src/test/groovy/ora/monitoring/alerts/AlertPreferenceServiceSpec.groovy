package ora.monitoring.alerts

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import ora.auth.User
import spock.lang.Specification

class AlertPreferenceServiceSpec extends Specification implements ServiceUnitTest<AlertPreferenceService>, DataTest {

    def setupSpec() {
        mockDomains(AlertPreference, User)
    }

    User testUser
    AlertPreference userPreference

    def setup() {
        // Créer un utilisateur de test
        testUser = new User(username: 'testuser', password: 'password').save(flush: true)

        // Créer un mock pour SpringSecurityService en utilisant les capacités de Mock de Spock
        def springSecurityServiceMock = Mock(SpringSecurityService)
        springSecurityServiceMock.getCurrentUser() >> testUser

        // Injecter le mock dans le service
        service.springSecurityService = springSecurityServiceMock

        // Créer une préférence de test pour l'utilisateur
        userPreference = new AlertPreference(
                emailEnabled: true,
                browserEnabled: false,
                systemEnabled: true,
                user: testUser
        ).save(flush: true)
    }

    def cleanup() {
        AlertPreference.list()*.delete(flush: true)
        User.list()*.delete(flush: true)
    }

    void "test getCurrentUser returns the authenticated user"() {
        when:
        def user = service.getCurrentUser()

        then:
        user == testUser
    }

    void "test getPreference returns the user preference"() {
        when:
        def pref = service.getPreference()

        then:
        pref != null
        pref.id == userPreference.id
        pref.user == testUser
    }

    void "test getPreferenceForUser returns preference for specific user"() {
        when:
        def pref = service.getPreferenceForUser(testUser)

        then:
        pref != null
        pref.id == userPreference.id
        pref.user == testUser
    }

    void "test getPreferenceById returns specific preference"() {
        when:
        def retrievedPref = service.getPreferenceById(userPreference.id)

        then:
        retrievedPref != null
        retrievedPref.id == userPreference.id
        retrievedPref.user == testUser
    }

    void "test createDefaultPreference creates new preference if none exists"() {
        given:
        // Supprimer la préférence existante
        userPreference.delete(flush: true)

        when:
        def newPref = service.createDefaultPreference()

        then:
        newPref != null
        newPref.id != null
        newPref.user == testUser
        newPref.browserEnabled
        !newPref.emailEnabled
        !newPref.systemEnabled
    }

    void "test createDefaultPreference returns existing preference if one exists"() {
        when:
        def pref = service.createDefaultPreference()

        then:
        pref != null
        pref.id == userPreference.id
        pref.user == testUser
    }

    void "test savePreference correctly saves a new preference"() {
        given:
        // Supprimer la préférence existante
        userPreference.delete(flush: true)

        def newPref = new AlertPreference(
                emailEnabled: true,
                browserEnabled: true,
                systemEnabled: true,
                user: testUser
        )

        when:
        def savedPref = newPref.save(flush: true, failOnError: true)

        then:
        savedPref != null
        savedPref.id != null
        savedPref.user == testUser
        AlertPreference.count() == 1
    }

    void "test updatePreference correctly updates an existing preference"() {
        given:
        def params = [
                emailEnabled  : false,
                browserEnabled: true,
                systemEnabled : false
        ]

        when:
        def updatedPref = service.updatePreference(userPreference.id, params)

        then:
        updatedPref != null
        updatedPref.id == userPreference.id
        !updatedPref.emailEnabled
        updatedPref.browserEnabled
        !updatedPref.systemEnabled
        updatedPref.user == testUser
    }

    void "test isAlertTypeEnabled correctly checks if a type is enabled"() {
        expect:
        service.isAlertTypeEnabled('email')
        !service.isAlertTypeEnabled('browser')
        service.isAlertTypeEnabled('system')
    }

    void "test getEmailRecipients returns configured recipients"() {
        given: "Une préférence avec des destinataires email"
        userPreference.emailRecipients = "admin@example.com, support@example.com"
        userPreference.save(flush: true)

        when: "On récupère les destinataires"
        def recipients = service.getEmailRecipients()

        then: "Les destinataires configurés sont retournés"
        recipients.size() == 2
        recipients.contains("admin@example.com")
        recipients.contains("support@example.com")
    }
}
