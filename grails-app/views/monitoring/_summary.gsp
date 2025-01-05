<%@ page import="java.time.LocalDateTime" %>
<div class="level is-mobile mb-0">
    <div class="level-left">
        <div id="summary-status" class="level-item has-text-centered">
            <div class="tags has-addons my-0">
                <span class="tag is-medium my-0">Applications</span>
                <span id="summary-status-count" class="tag is-medium my-0">-</span>
            </div>
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', () => {
        let problemsSet = new Set();
        let refreshCounter = 0;
        const totalCards = document.querySelectorAll('.card:not(#tab-hotspot .card)').length;

        // Écouter les événements de problème détecté
        MonitoringEventBus.on(MonitoringEventBus.EventTypes.PROBLEM_DETECTED, function (event) {
            if (!event.detail.app.status) {
                const key = event.detail.envId + '-' + event.detail.app.name;
                problemsSet.add(key);
            }
        });

        // Écouter les événements de refresh complet
        MonitoringEventBus.on(MonitoringEventBus.EventTypes.REFRESH_COMPLETED, function (event) {
            refreshCounter++;

            if (refreshCounter >= totalCards) {
                const statusCount = document.getElementById('summary-status-count');
                const totalDown = problemsSet.size;

                if (totalDown > 0) {
                    statusCount.textContent = "Down: " + totalDown;
                    statusCount.className = 'tag is-medium is-danger my-0';
                } else {
                    statusCount.textContent = 'Operational';
                    statusCount.className = 'tag is-medium is-success my-0';
                }

                // Réinitialiser pour le prochain cycle
                problemsSet.clear();
                refreshCounter = 0;
            }
        });
    });
</script>