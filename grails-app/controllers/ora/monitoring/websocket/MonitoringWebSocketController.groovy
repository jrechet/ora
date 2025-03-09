package ora.monitoring.websocket

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize

/**
 * Contrôleur dédié à l'endpoint WebSocket.
 * Ce contrôleur est explicitement configuré pour être accessible sans authentification.
 */
@Secured(['permitAll'])
class MonitoringWebSocketController {

    /**
     * Point d'entrée pour les connexions WebSocket.
     * Cette méthode ne fait rien car la gestion des WebSockets est assurée par le MonitoringWebSocketHandler.
     * Elle sert uniquement à définir un endpoint accessible sans authentification.
     */
    @Secured(['permitAll'])
    def index() {
        // Cette méthode ne fait rien, elle sert juste à définir un endpoint accessible
        corsHeaders()
        
        render(status: 200, text: "WebSocket endpoint")
    }
    
    /**
     * Point d'entrée alternatif pour les connexions WebSocket.
     */
    @Secured(['permitAll'])
    def connect() {
        // Cette méthode ne fait rien, elle sert juste à définir un endpoint accessible
        corsHeaders()
        
        render(status: 200, text: "WebSocket connect endpoint")
    }
    
    /**
     * Endpoint pour SockJS info request.
     * Nécessaire pour SockJS handshake.
     */
    @Secured(['permitAll'])
    def info() {
        corsHeaders()
        
        def info = [
            websocket: true,
            origins: ["*:*"],
            cookie_needed: false,
            entropy: (int)(Math.random() * 2147483647)
        ]
        
        render(contentType: "application/json", text: info as JSON)
    }
    
    /**
     * Endpoint SockJS pour les clients qui ne supportent pas WebSocket natif.
     */
    @Secured(['permitAll'])
    def sockjs() {
        corsHeaders()
        
        render(status: 200, text: "Welcome to SockJS!\n")
    }
    
    /**
     * Page de test pour les connexions WebSocket.
     * Cette page permet de tester manuellement les connexions WebSocket dans le navigateur.
     */
    @Secured(['permitAll'])
    def test() {
        corsHeaders()
        
        render(view: "/monitoring/websocket/test")
    }
    
    /**
     * Page de test simplifiée pour les connexions WebSocket.
     * Cette page utilise une configuration WebSocket plus simple.
     */
    @Secured(['permitAll'])
    def simpleTest() {
        corsHeaders()
        
        render(view: "/monitoring/websocket/simple_test")
    }
    
    /**
     * Helper method pour ajouter les CORS headers à la réponse
     */
    private void corsHeaders() {
        response.setHeader("Access-Control-Allow-Origin", "*")
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")
        response.setHeader("Access-Control-Allow-Credentials", "true")
        response.setHeader("Access-Control-Max-Age", "3600")
        
        // Gérer les requêtes preflight OPTIONS
        if (request.method == 'OPTIONS') {
            response.status = 200
            render(text: '')
            return
        }
    }
}
