package ora

class UrlMappings {
    static mappings = {
        "/$controller/$action?/$id?(.$format)?" {
            constraints {
                // apply constraints here
            }
        }

        "/"(controller: 'monitoring')
        "/private/open/healthCheck"(controller: 'healthCheck')
        "/auth/login"(controller: 'login', action: 'login')
        
        // Backoffice routes
        "/backoffice"(controller: 'backoffice', action: 'index')
        "/backoffice/alertPreference"(controller: 'alertPreference', action: 'index')
        "/backoffice/alertPreference/edit/$id?"(controller: 'alertPreference', action: 'index')
        "/backoffice/alertPreference/update/$id"(controller: 'alertPreference', action: 'update')
        
        // WebSocket test page
        "/websocket/test"(controller: 'monitoringWebSocket', action: 'test')
        
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
