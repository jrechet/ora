package ora

import grails.plugin.springsecurity.annotation.Secured

@Secured(['permitAll'])
class HealthCheckController {
    def index() {
        render 'ok'
    }
}
