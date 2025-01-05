const MonitoringConfig = {
    REFRESH_INTERVAL: 30000,
    MAX_TIME_WITHOUT_RESPONSE: 60000,
    STATUS_CHECK_TIMEOUT: 5000,
    COOKIE_NAME: 'monitoring_mode',
    COOKIE_EXPIRES_DAYS: 30,
    WS_RECONNECT_MAX_ATTEMPTS: 5,
    WS_RECONNECT_INTERVAL: 5000
};

// Exposer globalement pour être accessible par les autres modules
window.MonitoringConfig = MonitoringConfig;