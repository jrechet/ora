<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Test de connexion WebSocket</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
            line-height: 1.6;
        }
        .container {
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            border: 1px solid #ddd;
            border-radius: 5px;
        }
        .status {
            margin: 15px 0;
            padding: 10px;
            border-radius: 4px;
        }
        .success {
            background-color: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        .error {
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
        .info {
            background-color: #cce5ff;
            color: #004085;
            border: 1px solid #b8daff;
        }
        .log {
            height: 200px;
            overflow-y: auto;
            background-color: #f8f9fa;
            border: 1px solid #ddd;
            padding: 10px;
            margin-bottom: 20px;
        }
        button {
            background-color: #4CAF50;
            color: white;
            padding: 10px 15px;
            margin: 5px 0;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }
        button:hover {
            background-color: #45a049;
        }
        button:disabled {
            background-color: #cccccc;
            cursor: not-allowed;
        }
        .button-group {
            margin: 15px 0;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Test de connexion WebSocket ORA</h1>
        
        <div class="status info" id="status">État: Déconnecté</div>
        
        <div class="button-group">
            <button id="connectBtn">Connecter</button>
            <button id="disconnectBtn" disabled>Déconnecter</button>
        </div>
        
        <h2>Messages</h2>
        <div class="log" id="log"></div>
        
        <div class="button-group">
            <button id="sendMsgBtn" disabled>Envoyer un message de test</button>
        </div>
    </div>

    <!-- SockJS Library -->
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1.5.1/dist/sockjs.min.js"></script>
    
    <!-- For debugging -->
    <script>
        // Override WebSocket to log connection attempts
        (function() {
            var OrigWebSocket = window.WebSocket;
            var callWebSocket = function(url, protocols) {
                console.log("WebSocket attempt to: " + url);
                var ws = new OrigWebSocket(url, protocols);
                return ws;
            };
            window.WebSocket = callWebSocket;
        })();
    </script>
    
    <script>
        // Configuration
        const protocol = window.location.protocol === 'https:' ? 'https://' : 'http://';
        const wsBaseUrl = protocol + window.location.hostname + ':9999'; // à adapter si nécessaire
        const wsEndpoint = 'ws-endpoint';
        
        // Éléments DOM
        const statusEl = document.getElementById('status');
        const logEl = document.getElementById('log');
        const connectBtn = document.getElementById('connectBtn');
        const disconnectBtn = document.getElementById('disconnectBtn');
        const sendMsgBtn = document.getElementById('sendMsgBtn');
        
        // Variables
        let socket = null;
        
        // Fonctions d'affichage
        function updateStatus(message, type) {
            statusEl.textContent = 'État: ' + message;
            statusEl.className = 'status ' + type;
        }
        
        function logMessage(message, type = 'info') {
            const msgEl = document.createElement('div');
            msgEl.className = type;
            msgEl.textContent = new Date().toLocaleTimeString() + ' - ' + message;
            logEl.appendChild(msgEl);
            logEl.scrollTop = logEl.scrollHeight;
        }
        
        // Gestion des événements WebSocket
        function setupWebSocket() {
            try {
                // Utiliser SockJS au lieu de WebSocket natif
                const sockjsUrl = wsBaseUrl + '/' + wsEndpoint;
                logMessage('Tentative de connexion à ' + sockjsUrl + ' (via SockJS)');
                
                // Options SockJS pour améliorer la compatibilité
                const sockjsOptions = {
                    transports: ['websocket', 'xhr-streaming', 'xhr-polling'],
                    debug: true
                };
                
                socket = new SockJS(sockjsUrl, null, sockjsOptions);
                
                socket.onopen = function(event) {
                    updateStatus('Connecté', 'success');
                    logMessage('Connexion établie via SockJS', 'success');
                    logMessage('Mode de transport: ' + socket._transport.transportName, 'info');
                    connectBtn.disabled = true;
                    disconnectBtn.disabled = false;
                    sendMsgBtn.disabled = false;
                };
                
                socket.onmessage = function(event) {
                    logMessage('Message reçu: ' + event.data, 'info');
                };
                
                socket.onclose = function(event) {
                    let reason = event.reason || 'Raison inconnue';
                    if (event.code) {
                        reason += ' (code: ' + event.code + ')';
                    }
                    
                    updateStatus('Déconnecté: ' + reason, 'error');
                    logMessage('Connexion fermée: ' + reason, 'error');
                    resetUI();
                };
                
                // SockJS n'a pas d'événement onerror spécifique, les erreurs sont signalées via onclose
            } catch (error) {
                updateStatus('Erreur de connexion', 'error');
                logMessage('Exception: ' + error.message, 'error');
                console.error('Connection setup error:', error);
                resetUI();
            }
        }
        
        // Essayer aussi avec WebSocket natif
        function setupNativeWebSocket() {
            try {
                const wsUrl = 'ws://' + window.location.hostname + ':9999/' + wsEndpoint;
                logMessage('Tentative de connexion directe via WebSocket natif à ' + wsUrl);
                
                const wsSocket = new WebSocket(wsUrl);
                
                wsSocket.onopen = function(event) {
                    updateStatus('Connecté (WebSocket natif)', 'success');
                    logMessage('Connexion WebSocket natif établie', 'success');
                    socket = wsSocket;
                    connectBtn.disabled = true;
                    disconnectBtn.disabled = false;
                    sendMsgBtn.disabled = false;
                };
                
                wsSocket.onmessage = function(event) {
                    logMessage('Message reçu (WebSocket natif): ' + event.data, 'info');
                };
                
                wsSocket.onclose = function(event) {
                    if (socket === wsSocket) {
                        let reason = 'Code: ' + event.code;
                        updateStatus('Déconnecté (WebSocket natif): ' + reason, 'error');
                        logMessage('Connexion WebSocket natif fermée: ' + reason, 'error');
                        resetUI();
                        
                        // Si la connexion WebSocket native échoue, essayer SockJS
                        setTimeout(setupWebSocket, 1000);
                    }
                };
                
                wsSocket.onerror = function(error) {
                    logMessage('Erreur WebSocket natif', 'error');
                    console.error('Native WebSocket Error:', error);
                    
                    // Échec du WebSocket natif, fallback vers SockJS
                    if (socket === wsSocket) {
                        logMessage('Échec du WebSocket natif, tentative de connexion SockJS...', 'info');
                        wsSocket.close();
                    }
                };
            } catch (error) {
                logMessage('Exception WebSocket natif: ' + error.message, 'error');
                console.error('Native WebSocket error:', error);
                
                // Fallback vers SockJS
                setTimeout(setupWebSocket, 1000);
            }
        }
        
        function sendTestMessage() {
            if (socket && socket.readyState === 1) {  // OPEN state
                const testMsg = JSON.stringify({
                    type: 'test',
                    data: 'Message de test ' + new Date().toISOString()
                });
                socket.send(testMsg);
                logMessage('Message envoyé: ' + testMsg);
            } else {
                logMessage('Impossible d\'envoyer le message: non connecté', 'error');
            }
        }
        
        function closeConnection() {
            if (socket) {
                socket.close();
                socket = null;
            }
        }
        
        function resetUI() {
            connectBtn.disabled = false;
            disconnectBtn.disabled = true;
            sendMsgBtn.disabled = true;
        }
        
        // Event listeners
        connectBtn.addEventListener('click', function() {
            // Utiliser directement SockJS (plus fiable)
            setupWebSocket();
        });
        disconnectBtn.addEventListener('click', closeConnection);
        sendMsgBtn.addEventListener('click', sendTestMessage);
        
        // Info initiale
        logMessage('Page chargée. Cliquez sur "Connecter" pour démarrer');
    </script>
</body>
</html>