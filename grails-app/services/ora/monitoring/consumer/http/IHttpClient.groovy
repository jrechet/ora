package ora.monitoring.consumer.http

/**
 * Interface commune pour les clients HTTP
 */
interface IHttpClient {
    String getForString(String url, String headers)
    
    /**
     * Vérifie si un service est en bonne santé en vérifiant le code HTTP 200
     * @param url L'URL du service à vérifier
     * @return true si le service répond avec un code HTTP 200, false sinon
     */
    boolean isHealthy(String url)

    boolean isAvailable()

    void handleResponse(String message)
}