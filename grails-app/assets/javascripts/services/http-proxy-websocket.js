const HttpProxyWebSocket = {
    socket: null,
    reconnectAttempts: 0,
    maxReconnectAttempts: 5,
    isConnected: false,

    initialize: function() {
        this.connect();
    },

    notifyWebSocketStatus: function(available) {
        MonitoringEventBus.emit(MonitoringEventBus.EventTypes.WEBSOCKET_STATUS, {
            available: available
        });
    },

    connect: function() {
        try {
            const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
            this.socket = new WebSocket(wsProtocol + '//' + window.location.host + '/ws-endpoint');

            // Bind des méthodes avec le bon contexte
            this.socket.onopen = this.onConnect.bind(this);
            this.socket.onclose = this.onError.bind(this);
            this.socket.onerror = this.onWebSocketError.bind(this);
            this.socket.onmessage = this.onMessage.bind(this);
        } catch (error) {
            console.error('Error connecting to WebSocket:', error);
            this.handleConnectionError();
        }
    },

    onConnect: function() {
        this.isConnected = true;
        this.reconnectAttempts = 0;
        console.log('Connected to WebSocket proxy');
        this.notifyWebSocketStatus(true);
    },

    onError: function() {
        this.isConnected = false;
        this.notifyWebSocketStatus(false);
        this.handleConnectionError();
    },

    onWebSocketError: function(error) {
        console.error('WebSocket error:', error);
        this.notifyWebSocketStatus(false);
    },

    handleConnectionError: function() {
        let self = this;
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            setTimeout(function() {
                self.connect();
            }, 5000 * this.reconnectAttempts);
        } else {
            console.error('Max reconnection attempts reached');
        }
    },

    onMessage: function(event) {
        try {
            let message;
            try {
                message = JSON.parse(event.data);
            } catch (e) {
                console.log('Received non-JSON message:', event.data);
                return;
            }

            if (!this.validateMessage(message)) {
                return;
            }

            this.handleMessage(message);

        } catch (error) {
            console.error('Error processing message:', error);
        }
    },

    validateMessage: function(message) {
        if (!message || typeof message !== 'object' || !message.type) {
            console.warn('Invalid message format:', message);
            return false;
        }

        switch (message.type) {
            case 'HTTP_REQUEST':
                return this.validateHttpRequest(message);
            case 'PING':
                return this.validatePing(message);
            default:
                console.warn('Unknown message type:', message.type);
                return false;
        }
    },

    validateHttpRequest: function(message) {
        if (!message.url) {
            console.warn('HTTP_REQUEST missing url:', message);
            return false;
        }
        if (!message.requestId) {
            console.warn('HTTP_REQUEST missing requestId:', message);
            return false;
        }
        return true;
    },

    validatePing: function(message) {
        if (!message.timestamp) {
            console.warn('PING missing timestamp:', message);
            return false;
        }
        return true;
    },

    handleMessage: function(message) {
        switch (message.type) {
            case 'HTTP_REQUEST':
                this.handleHttpRequest(message);
                break;
            case 'PING':
                this.handlePing(message);
                break;
            default:
                console.log('Unknown message type:', message.type);
                break;
        }
    },

    handlePing: function(message) {
        if (this.canSendMessage()) {
            this.sendMessage({
                type: 'PONG',
                timestamp: message.timestamp
            });
            console.debug('Sent PONG response');
        }
    },

    handleHttpRequest: function(message) {
        const self = this;
        const options = this.buildRequestOptions(message.headers);

        fetch(message.url, options)
            .then(function(response) {
                return response.text().then(function(data) {
                    return {
                        status: response.status,
                        data: data
                    };
                });
            })
            .then(function(result) {
                self.sendHttpResponse(message.requestId, result.status, result.data);
            })
            .catch(function(error) {
                self.sendHttpResponse(message.requestId, 0, null, error.message);
            });
    },

    buildRequestOptions: function(headers) {
        if (!headers) {
            return { method: 'GET' };
        }

        const options = {
            method: 'GET',
            headers: {}
        };

        const headerPairs = headers.split('\n');
        for (let i = 0; i < headerPairs.length; i++) {
            const parts = headerPairs[i].split(':');
            if (parts.length === 2) {
                options.headers[parts[0].trim()] = parts[1].trim();
            }
        }

        return options;
    },

    sendHttpResponse: function(requestId, status, data, error) {
        if (this.canSendMessage()) {
            const response = {
                type: 'HTTP_RESPONSE',
                requestId: requestId,
                status: status
            };

            if (error) {
                response.error = error;
            } else {
                response.data = data;
            }

            this.sendMessage(response);
        }
    },

    sendMessage: function(message) {
        if (this.canSendMessage()) {
            this.socket.send(JSON.stringify(message));
        }
    },

    canSendMessage: function() {
        return this.socket && this.isConnected;
    }
};

// Initialisation au chargement de la page
document.addEventListener('DOMContentLoaded', function() {
    HttpProxyWebSocket.initialize();
});

window.HttpProxyWebSocket = HttpProxyWebSocket;