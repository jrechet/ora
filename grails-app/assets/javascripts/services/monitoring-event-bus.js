let MonitoringEventBus = {
    events: new Map(),

    // Création d'un événement personnalisé
    createEvent: function(eventName, detail) {
        return new CustomEvent(eventName, {
            bubbles: true,
            detail: detail
        });
    },

    // Émission d'un événement
    emit: function(eventName, detail) {
        document.dispatchEvent(this.createEvent(eventName, detail));
        console.debug('Event emitted:', eventName, detail);
    },

    // Abonnement à un événement
    on: function(eventName, callback) {
        if (!this.events.has(eventName)) {
            this.events.set(eventName, new Set());
        }
        this.events.get(eventName).add(callback);
        document.addEventListener(eventName, callback);
    },

    // Désabonnement d'un événement
    off: function(eventName, callback) {
        if (this.events.has(eventName)) {
            this.events.get(eventName).delete(callback);
            document.removeEventListener(eventName, callback);
        }
    },

    // Désabonnement de tous les événements d'un type
    clearEventListeners: function(eventName) {
        if (this.events.has(eventName)) {
            this.events.get(eventName).forEach(function(callback) {
                document.removeEventListener(eventName, callback);
            });
            this.events.delete(eventName);
        }
    },

    // Liste des événements standards du système
    EventTypes: {
        MODE_CHANGED: 'monitoring-mode-changed',
        REFRESH_REQUESTED: 'monitoring-refresh-requested',
        REFRESH_COMPLETED: 'monitoring-refresh-completed',
        PROBLEM_DETECTED: 'monitoring-problem-detected',
        CONNECTION_STATUS: 'monitoring-connection-status',
        WEBSOCKET_STATUS: 'monitoring-websocket-status',
        UI_UPDATE: 'monitoring-ui-update'
    }
};

window.MonitoringEventBus = MonitoringEventBus;