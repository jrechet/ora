// status-summary-manager.js
const StatusSummaryManager = {
    state: {
        problemsSet: new Set(),
        refreshCounter: 0,
        totalCards: 0,
        totalProblems: 0
    },

    initialize: function() {
        this.state.totalCards = document.querySelectorAll('.card:not(#tab-hotspot .card)').length;
        this.setupEventListeners();
        this.resetState();
    },

    setupEventListeners: function() {
        MonitoringEventBus.on(MonitoringEventBus.EventTypes.PROBLEM_DETECTED, function(event) {
            this.handleProblemDetected(event.detail);
        }.bind(this));

        MonitoringEventBus.on(MonitoringEventBus.EventTypes.REFRESH_COMPLETED, function() {
            this.handleRefreshCompleted();
        }.bind(this));
    },

    handleProblemDetected: function(detail) {
        if (!detail.app.status) {
            const key = detail.envId + '-' + detail.app.name;
            this.state.problemsSet.add(key);
            this.state.totalProblems = this.state.problemsSet.size;
            this.updateSummaryStatus();
        }
    },

    handleRefreshCompleted: function() {
        this.state.refreshCounter++;

        if (this.state.refreshCounter >= this.state.totalCards) {
            this.updateSummaryStatus();
            this.resetState();
        }
    },

    updateSummaryStatus: function() {
        const statusCount = document.getElementById('summary-status-count');
        if (!statusCount) return;

        const totalDown = this.state.totalProblems;

        if (totalDown > 0) {
            statusCount.textContent = 'Down: ' + totalDown;
            statusCount.className = 'tag is-medium is-danger my-0';
        } else {
            statusCount.textContent = 'Operational';
            statusCount.className = 'tag is-medium is-success my-0';
        }
    },

    resetState: function() {
        this.state.problemsSet.clear();
        this.state.refreshCounter = 0;
        this.state.totalProblems = 0;
    }
};

document.addEventListener('DOMContentLoaded', function() {
    StatusSummaryManager.initialize();
});

window.StatusSummaryManager = StatusSummaryManager;