package ora

/**
 * Contrôleur simple pour tester les WebSockets sans Spring Security
 */
class WebSocketTestController {

    /**
     * Affiche la page de test WebSocket
     */
    def index() {
        render(view: "/websocketTest")
    }
}