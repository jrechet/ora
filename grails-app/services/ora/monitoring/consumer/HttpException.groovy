package ora.monitoring.consumer

class HttpException extends RuntimeException {
    HttpException(String message) {
        super(message)
    }

    HttpException(String message, Throwable cause) {
        super(message, cause)
    }
}