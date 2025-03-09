const WebSocket = require('ws');

console.log('Attempting to connect to WebSocket...');

const ws = new WebSocket('ws://localhost:9999/ws-endpoint');

ws.on('open', function open() {
  console.log('Connection established!');
  
  ws.send(JSON.stringify({
    type: 'PING',
    timestamp: Date.now()
  }));
  
  console.log('Sent ping message');
});

ws.on('message', function incoming(data) {
  console.log('Received:', data.toString());
  ws.close();
});

ws.on('error', function error(err) {
  console.error('WebSocket error:', err.message);
});

ws.on('close', function close(code, reason) {
  console.log(`Connection closed. Code: ${code}, Reason: ${reason}`);
});

// Close after 5 seconds if no activity
setTimeout(() => {
  if (ws.readyState === WebSocket.OPEN) {
    console.log('Closing connection due to timeout');
    ws.close();
  }
  process.exit(0);
}, 5000);