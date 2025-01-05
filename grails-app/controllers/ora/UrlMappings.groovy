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
        "500"(view: '/error')
        "404"(view: '/notFound')

    }
}
