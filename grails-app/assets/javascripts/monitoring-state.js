// monitoring-state.js
const MonitoringState = {
    currentMode: 'server',
    isWebSocketAvailable: false,
    lastSuccessfulResponse: Date.now(),
    problemsMap: new Map(),
    refreshCounter: 0,

    initialize: function() {
        this.setupEventListeners();
    },

    setupEventListeners: function() {
        MonitoringEventBus.on(MonitoringEventBus.EventTypes.MODE_CHANGED, (event) => {
            this.currentMode = event.detail.mode;
        });

        MonitoringEventBus.on(MonitoringEventBus.EventTypes.WEBSOCKET_STATUS, (event) => {
            this.isWebSocketAvailable = event.detail.available;
        });

        MonitoringEventBus.on(MonitoringEventBus.EventTypes.PROBLEM_DETECTED, (event) => {
            this.handleProblemDetected(event.detail);
        });

        MonitoringEventBus.on(MonitoringEventBus.EventTypes.REFRESH_COMPLETED, (event) => {
            this.handleRefreshCompleted(event.detail);
        });
    },

    handleProblemDetected: function(problemDetails) {
        const key = problemDetails.envName + '-' + problemDetails.tenant;
        if (!this.problemsMap.has(key)) {
            this.problemsMap.set(key, {
                envName: problemDetails.envName,
                tenant: problemDetails.tenant,
                apps: new Set(),
                environmentLevel: problemDetails.environmentLevel
            });
        }
        this.problemsMap.get(key).apps.add(problemDetails.app);
    },

    handleRefreshCompleted: function(details) {
        this.refreshCounter++;
        this.lastSuccessfulResponse = Date.now();

        const totalCards = document.querySelectorAll('.card:not(#tab-hotspot .card)').length;

        if (this.refreshCounter >= totalCards) {
            const problems = [];
            this.problemsMap.forEach((envData, key) => {
                envData.apps.forEach(app => {
                    problems.push({
                        app: app,
                        envName: envData.envName,
                        tenant: envData.tenant,
                        environmentLevel: envData.environmentLevel
                    });
                });
            });

            MonitoringEventBus.emit(MonitoringEventBus.EventTypes.UI_UPDATE, {
                type: 'hotspot',
                problems: problems
            });

            this.refreshCounter = 0;
            this.problemsMap.clear();
        }
    }
};

window.MonitoringState = MonitoringState;