package ora.monitoring.websocket

import grails.plugin.springsecurity.annotation.Secured

/**
 * Contrôleur minimal pour les tests WebSocket uniquement.
 * Ce contrôleur ne gère PAS les endpoints WebSocket eux-mêmes.
 */
@Secured(['permitAll'])
class MonitoringWebSocketController {
    
    /**
     * Page de test pour les connexions WebSocket.
     */
    @Secured(['permitAll'])
    def test() {
        addCorsHeaders()
        render(view: "/websocketTest")
    }
    
    /**
     * Helper method pour les headers CORS
     */
    private void addCorsHeaders() {
        response.setHeader("Access-Control-Allow-Origin", "*")
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
    }
}
