package ora.auth

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured

@Secured('permitAll')
class LoginController {
    def springSecurityService

    def login() {
        def config = SpringSecurityUtils.securityConfig

        if (springSecurityService.isLoggedIn()) {
            redirect uri: config.successHandler.defaultTargetUrl
            return
        }

        render view: '/auth/login', model: [
            postUrl: "${request.contextPath}${config.apf.filterProcessesUrl}",
            rememberMeParameter: config.rememberMe.parameter
        ]
    }

    def denied() {
        render view: '/auth/denied'
    }
}
