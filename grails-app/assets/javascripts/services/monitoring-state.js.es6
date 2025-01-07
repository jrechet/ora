const MonitoringState = {
    currentMode: 'server',
    isWebSocketAvailable: false,
    lastSuccessfulResponse: Date.now(),
    problemsMap: new Map(),
    refreshCounter: 0,

    initialize: function () {
        this.setupEventListeners();
    },

    setupEventListeners: function () {
        MonitoringEventBus.on(MonitoringEventBus.EventTypes.MODE_CHANGED, function (event) {
            this.currentMode = event.detail.mode;
        }.bind(this));

        MonitoringEventBus.on(MonitoringEventBus.EventTypes.WEBSOCKET_STATUS, function (event) {
            this.isWebSocketAvailable = event.detail.available;
        }.bind(this));

        MonitoringEventBus.on(MonitoringEventBus.EventTypes.PROBLEM_DETECTED, function (event) {
            this.handleProblemDetected(event.detail);
        }.bind(this));

        MonitoringEventBus.on(MonitoringEventBus.EventTypes.REFRESH_COMPLETED, function (event) {
            this.handleRefreshCompleted(event.detail);
        }.bind(this));
    },

    handleProblemDetected: function (problemDetails) {
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

    handleRefreshCompleted: function (details) {
        this.refreshCounter++;
        this.lastSuccessfulResponse = Date.now();

        const totalCards = document.querySelectorAll('.card:not(#tab-hotspot .card)').length;

        if (this.refreshCounter >= totalCards) {
            const problems = this.collectProblems();
            MonitoringEventBus.emit(MonitoringEventBus.EventTypes.UI_UPDATE, {
                type: 'hotspot',
                problems: problems
            });

            this.resetState();
        }
    },

    collectProblems: function () {
        const problems = [];
        this.problemsMap.forEach(function (envData, key) {
            envData.apps.forEach(function (app) {
                problems.push({
                    app: app,
                    envName: envData.envName,
                    tenant: envData.tenant,
                    environmentLevel: envData.environmentLevel
                });
            });
        });
        return problems;
    },

    resetState: function () {
        this.refreshCounter = 0;
        this.problemsMap.clear();
    }
};

// Initialisation au chargement de la page
document.addEventListener('DOMContentLoaded', function () {
    MonitoringState.initialize();
});

window.MonitoringState = MonitoringState;