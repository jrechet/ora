<div class="card" data-env-id="${environment.id}" data-env-level="${environment.level}">
    <header class="card-header" data-logical-name="${environment.logicalName}" data-tenant="${environment.tenant}">
        <p class="card-header-title">
            ${environment.logicalName.capitalize()}
        </p>
    </header>

    <div class="card-content">
        <table class="table is-fullwidth monitoring-table">
            <tbody>
            <g:each in="${applications.sort { it.name }}" var="app">
                <g:render template="/monitoring/appRow" model="[app: app]"/>
            </g:each>
            </tbody>
        </table>
    </div>

    <footer class="card-footer">
        <div class="card-footer-item">
            Last check: <span class="last-check ml-2 is-size-7">${applications.first()?.lastChecked ?: ''}</span>
        </div>
    </footer>
</div>