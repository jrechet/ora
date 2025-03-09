<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Test WebSocket Simplifié</title>
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
        .radio-group {
            margin: 15px 0;
        }
        .radio-group label {
            margin-right: 15px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Test WebSocket Simplifié ORA</h1>
        
        <div class="status info" id="status">État: Déconnecté</div>
        
        <div class="radio-group">
            <label>
                <input type="radio" name="endpoint" value="native" checked> WebSocket natif
            </label>
            <label>
                <input type="radio" name="endpoint" value="sockjs"> SockJS
            </label>
        </div>
        
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

    <script>
        // Éléments DOM
        const statusEl = document.getElementById('status');
        const logEl = document.getElementById('log');
        const connectBtn = document.getElementById('connectBtn');
        const disconnectBtn = document.getElementById('disconnectBtn');
        const sendMsgBtn = document.getElementById('sendMsgBtn');
        const endpointRadios = document.getElementsByName('endpoint');
        
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
        
        // Obtenir le type d'endpoint sélectionné
        function getSelectedEndpoint() {
            for (let i = 0; i < endpointRadios.length; i++) {
                if (endpointRadios[i].checked) {
                    return endpointRadios[i].value;
                }
            }
            return 'native'; // par défaut
        }
        
        // Établir la connexion WebSocket
        function setupWebSocket() {
            try {
                const hostname = window.location.hostname;
                const port = '9999';
                const endpointType = getSelectedEndpoint();
                
                closeConnection(); // fermer la connexion existante si présente
                
                if (endpointType === 'native') {
                    // WebSocket natif
                    const wsUrl = 'ws://' + hostname + ':' + port + '/monitoring-ws';
                    logMessage('Tentative de connexion WebSocket natif à ' + wsUrl);
                    
                    socket = new WebSocket(wsUrl);
                } else {
                    // SockJS
                    const sockjsUrl = 'http://' + hostname + ':' + port + '/monitoring-ws-sockjs';
                    logMessage('Tentative de connexion SockJS à ' + sockjsUrl);
                    
                    // Si SockJS était chargé, nous l'utiliserions ici
                    logMessage('SockJS non disponible, utilisation d\'une connexion WebSocket directe');
                    socket = new WebSocket('ws://' + hostname + ':' + port + '/monitoring-ws-sockjs');
                }
                
                // Configuration des événements
                socket.onopen = function(event) {
                    updateStatus('Connecté', 'success');
                    logMessage('Connexion établie', 'success');
                    connectBtn.disabled = true;
                    disconnectBtn.disabled = false;
                    sendMsgBtn.disabled = false;
                };
                
                socket.onmessage = function(event) {
                    logMessage('Message reçu: ' + event.data, 'info');
                };
                
                socket.onclose = function(event) {
                    let reason = 'Code: ' + event.code;
                    if (event.reason) {
                        reason += ', Raison: ' + event.reason;
                    }
                    
                    updateStatus('Déconnecté: ' + reason, 'error');
                    logMessage('Connexion fermée: ' + reason, 'error');
                    resetUI();
                };
                
                socket.onerror = function(error) {
                    logMessage('Erreur WebSocket', 'error');
                    console.error('WebSocket Error:', error);
                };
            } catch (error) {
                updateStatus('Erreur de connexion', 'error');
                logMessage('Exception: ' + error.message, 'error');
                console.error('Connection setup error:', error);
                resetUI();
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
        connectBtn.addEventListener('click', setupWebSocket);
        disconnectBtn.addEventListener('click', closeConnection);
        sendMsgBtn.addEventListener('click', sendTestMessage);
        
        // Info initiale
        logMessage('Page chargée. Sélectionnez un type de connexion et cliquez sur "Connecter" pour démarrer');
    </script>
</body>
</html>