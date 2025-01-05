<%@ page import="ora.monitoring.apps.Environment" %>
<meta name="layout" content="monitoring"/>

<div class="section">
    <%
        // Grouper les environnements par tenant
        def groupedEnvs = [:]
        applicationStructure.each { envId, applications ->
            def env = Environment.get(envId)
            if (!groupedEnvs[env.tenant]) {
                groupedEnvs[env.tenant] = [:]
            }
            groupedEnvs[env.tenant][env] = applications
        }

        // Pour chaque groupe, trier les environnements par level
        groupedEnvs.each { tenant, envs ->
            groupedEnvs[tenant] = envs.sort { a, b ->
                def levelA = a.key.level
                def levelB = b.key.level
                levelA <=> levelB
            }
        }
    %>

    <div class="tabs">
        <ul>
            <li class="is-active">
                <a href="#tab-hotspot">Hotspot</a>
            </li>
            <g:each in="${groupedEnvs.keySet()}" var="tabName">
                <li>
                    <a href="#tab-${tabName}">${tabName}</a>
                </li>
            </g:each>
        </ul>
    </div>

    <!-- Hotspot tab (sans données initiales, sera rempli par JS) -->
    <g:render template="/monitoring/hotspot" model="[
            active             : true,
            problemApplications: []
    ]"/>

    <!-- Tenant tabs -->
    <g:each in="${groupedEnvs}" var="groupEntry">
        <g:render template="/monitoring/tenant" model="[
                tenant      : groupEntry.key,
                environments: groupEntry.value,
                active      : false
        ]"/>
    </g:each>
</div>

<style>
.tab-content {
    display: none;
}

.tab-content.is-active {
    display: block;
}
</style>