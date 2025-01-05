package ora.taglib

class ApplicationTagLib {
    static namespace = "kf"

    def createTestStatusTooltip = { attrs ->
        def app = attrs.application
        def messages = []

        messages << "Tests"
        messages << "Unit tests: ${app.unitTestsSuccess ? '✓' : '✗'}"
        messages << "Integration tests: ${app.integrationTestsSuccess ? '✓' : '✗'}"

        out << messages.join('\n')
    }
}