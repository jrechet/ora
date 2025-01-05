const express = require('express');
const cors = require('cors');
const app = express();
const PORT = process.env.PORT || 3000;

// Enable CORS for all routes
const corsOptions = {
    origin: true, // Allows all origins
    methods: ['GET', 'HEAD', 'PUT', 'PATCH', 'POST', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'Authorization'],
    credentials: true,
    preflightContinue: false,
    optionsSuccessStatus: 204
};

app.use(cors(corsOptions));

// Fonction pour obtenir un status aléatoire
function getRandomStatus() {
    return Math.random() > 0.7 ? 500 : 200;
}

// État global des services
let statusCodes = {
    healthcheck: parseInt(process.env.HEALTH_STATUS) || 200,
    monitoring: parseInt(process.env.MONITORING_STATUS) || 200,
    supervision: parseInt(process.env.SUPERVISION_STATUS) || 200
};

// Fonction pour mettre à jour aléatoirement les status
function updateRandomStatus() {
    statusCodes = {
        healthcheck: process.env.HEALTH_STATUS ? parseInt(process.env.HEALTH_STATUS) : getRandomStatus(),
        monitoring: process.env.MONITORING_STATUS ? parseInt(process.env.MONITORING_STATUS) : getRandomStatus(),
        supervision: process.env.SUPERVISION_STATUS ? parseInt(process.env.SUPERVISION_STATUS) : getRandomStatus()
    };
    console.log('Status codes:', statusCodes);
}

// Mise à jour des status toutes les 5 secondes uniquement pour les services non configurés
if (!process.env.HEALTH_STATUS || !process.env.MONITORING_STATUS || !process.env.SUPERVISION_STATUS) {
    setInterval(updateRandomStatus, 10000);
}

app.get('/healthcheck', (req, res) => {
    // Set CORS headers explicitly for this endpoint
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET, OPTIONS');
    res.header('Access-Control-Allow-Headers', 'Content-Type');

    res.status(statusCodes.healthcheck).json({
        status: statusCodes.healthcheck === 200 ? 'OK' : 'KO',
        timestamp: new Date().toISOString()
    });
});

app.get('/monitoring', (req, res) => {
    // Set CORS headers explicitly for this endpoint
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET, OPTIONS');
    res.header('Access-Control-Allow-Headers', 'Content-Type');

    res.status(statusCodes.monitoring).json({
        status: statusCodes.monitoring === 200 ? 'OK' : 'KO',
        timestamp: new Date().toISOString()
    });
});

app.get('/supervision', (req, res) => {
    // Set CORS headers explicitly for this endpoint
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET, OPTIONS');
    res.header('Access-Control-Allow-Headers', 'Content-Type');

    const status = statusCodes.supervision;

    const services = [
        {
            id: 1,
            name: 'Database Connection',
            status: Math.random() > 0.7 ? 'KO' : 'OK'
        },
        {
            id: 2,
            name: 'Cache Service',
            status: Math.random() > 0.7 ? 'KO' : 'OK'
        },
        {
            id: 3,
            name: 'Authentication Service',
            status: Math.random() > 0.7 ? 'KO' : 'OK'
        },
        {
            id: 4,
            name: 'Email Service',
            status: Math.random() > 0.7 ? 'KO' : 'OK'
        }
    ];

    const html = `
        <html>
        <body>
            <table>
                <tbody>
                    ${services.map(service => `
                        <tr id="V${service.id}">
                            <td>V${service.id}&nbsp;:&nbsp;${service.name}&nbsp;=&nbsp;url&nbsp;test</td>
                            <td class="${service.status.toLowerCase()}">${service.status}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        </body>
        </html>
    `;

    res.status(status).send(html);
});

// Handle OPTIONS requests explicitly
app.options('*', cors(corsOptions));

app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
    console.log('Initial status codes:', statusCodes);
});