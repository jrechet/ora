package ora.monitoring.alerts

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class AlertPreferenceSpec extends Specification implements DomainUnitTest<AlertPreference> {

    def setup() {
    }

    def cleanup() {
    }

    void "test default values"() {
        when:
        def preference = new AlertPreference()

        then:
        !preference.emailEnabled
        preference.browserEnabled
        !preference.systemEnabled
        preference.active
        preference.name == "Default"
    }

    void "test constraints validation"() {
        when: "A preference is created with no name"
        def preference = new AlertPreference(name: null)

        then: "Validation should fail"
        !preference.validate()
        preference.errors['name']

        when: "A preference is created with an empty name"
        preference = new AlertPreference(name: "")

        then: "Validation should fail"
        !preference.validate()
        preference.errors['name']

        when: "A preference is created with a valid name"
        preference = new AlertPreference(name: "Test Config")

        then: "Validation should pass"
        preference.validate()
    }

    void "test unique constraint on name"() {
        given: "An existing preference"
        def preference1 = new AlertPreference(name: "Config 1").save(flush: true)

        when: "A new preference with the same name is created"
        def preference2 = new AlertPreference(name: "Config 1")

        then: "Validation should fail due to unique constraint"
        !preference2.validate()
        preference2.errors['name']
    }

    void "test getActivePreference when active preference exists"() {
        given: "An active preference exists"
        def existingPref = new AlertPreference(name: "Active Config", active: true).save(flush: true)

        when: "getActivePreference is called"
        def activePref = AlertPreference.getActivePreference()

        then: "The existing active preference should be returned"
        activePref != null
        activePref.id == existingPref.id
        activePref.name == "Active Config"
    }

    void "test getActivePreference when no active preference exists"() {
        given: "No preferences exist"
        AlertPreference.list()*.delete(flush: true)

        when: "getActivePreference is called"
        def activePref = AlertPreference.getActivePreference()

        then: "A new default preference should be created and returned"
        activePref != null
        activePref.id != null
        activePref.name == "Default"
        activePref.active
        AlertPreference.count() == 1
    }

    void "test persistence of AlertPreference"() {
        given: "A preference with specific settings"
        def preference = new AlertPreference(
            name: "Test Persistence",
            emailEnabled: true,
            browserEnabled: false,
            systemEnabled: true
        )

        when: "The preference is saved"
        preference.save(flush: true)
        def id = preference.id

        and: "The preference is retrieved from the database"
        def retrievedPref = AlertPreference.get(id)

        then: "The retrieved preference should match the original settings"
        retrievedPref != null
        retrievedPref.name == "Test Persistence"
        retrievedPref.emailEnabled
        !retrievedPref.browserEnabled
        retrievedPref.systemEnabled
    }
}
