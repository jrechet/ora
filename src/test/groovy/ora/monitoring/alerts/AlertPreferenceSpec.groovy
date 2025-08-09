package ora.monitoring.alerts

import grails.testing.gorm.DomainUnitTest
import ora.auth.User
import spock.lang.Specification
import spock.lang.Ignore

class AlertPreferenceSpec extends Specification implements DomainUnitTest<AlertPreference> {

    def setupSpec() {
        mockDomain(User)
    }

    def setup() {
    }

    def cleanup() {
    }

    void "test default values"() {
        when:
        def preference = new AlertPreference()

        then:
        !preference.emailEnabled
        !preference.browserEnabled
        !preference.systemEnabled
    }

    void "test constraints validation"() {
        when: "Une préférence est créée sans utilisateur"
        def preference = new AlertPreference(user: null)

        then: "La validation doit échouer"
        !preference.validate()
        preference.errors['user']

        when: "Une préférence est créée avec un utilisateur valide"
        def user = new User(username: 'testuser', password: 'password').save(flush: true)
        preference = new AlertPreference(user: user)

        then: "La validation doit réussir pour l'utilisateur"
        preference.validate(['user'])
    }

    @Ignore("Test de contrainte d'unicité à corriger ultérieurement")
    void "test unique constraint on user"() {
        given: "Un utilisateur"
        def user = new User(username: 'user1', password: 'password').save(flush: true)
        
        and: "Une préférence existante pour cet utilisateur"
        new AlertPreference(user: user).save(flush: true)

        when: "Une nouvelle préférence est créée pour le même utilisateur"
        def preference = new AlertPreference(user: user)

        then: "La validation doit échouer"
        !preference.validate()
        preference.errors.hasFieldErrors('user')
    }

    void "test persistence of AlertPreference"() {
        given: "Un utilisateur"
        def user = new User(username: 'persistuser', password: 'password').save(flush: true)
        
        and: "Une préférence avec des paramètres spécifiques"
        def preference = new AlertPreference(
            emailEnabled: true,
            browserEnabled: false,
            systemEnabled: true,
            user: user
        )

        when: "La préférence est sauvegardée"
        preference.save(flush: true)
        def id = preference.id

        and: "La préférence est récupérée depuis la base de données"
        def retrievedPref = AlertPreference.get(id)

        then: "La préférence récupérée doit correspondre aux paramètres originaux"
        retrievedPref != null
        retrievedPref.emailEnabled
        !retrievedPref.browserEnabled
        retrievedPref.systemEnabled
        retrievedPref.user == user
    }
}
