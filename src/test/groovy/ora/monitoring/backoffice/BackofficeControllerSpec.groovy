package ora.monitoring.backoffice

import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class BackofficeControllerSpec extends Specification implements ControllerUnitTest<BackofficeController> {

    def setup() {
    }

    def cleanup() {
    }

    void "test index action returns correct model"() {
        when: "La méthode index est appelée"
        def model = controller.index()

        then: "Le modèle contient les bonnes valeurs"
        model.pageTitle == "Tableau de bord"
        model.currentSection == "dashboard"
    }
}
