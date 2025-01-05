package ora.monitoring.consumer.http

/**
 * Interface commune pour les clients HTTP
 */
interface HttpClient {
    String getForString(String url, String headers)

    boolean isAvailable()

    void handleResponse(String message)
}
