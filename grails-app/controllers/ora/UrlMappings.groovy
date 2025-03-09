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
        
        // Explicit WebSocket endpoints
        "/monitoring-ws"(controller: 'monitoringWebSocket', action: 'index')
        "/monitoring-ws/connect"(controller: 'monitoringWebSocket', action: 'connect')
        "/monitoring-ws/sockjs"(controller: 'monitoringWebSocket', action: 'sockjs')
        "/monitoring-ws/info"(controller: 'monitoringWebSocket', action: 'info')
        "/monitoring-ws/test"(controller: 'monitoringWebSocket', action: 'test')
        "/monitoring-ws-test"(controller: 'monitoringWebSocket', action: 'simpleTest')
        
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
