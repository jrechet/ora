<%-- _appRow.gsp --%>
<tr>
    <td>
        <span class="has-text-weight-medium is-size-6">
            <g:if test="${app.baseUrl}">
                <a href="${app.baseUrl}"
                   target="_blank"
                   class="text-dark text-decoration-none">
                    <nobr>${app.name}</nobr>
                </a>
            </g:if>
            <g:else>
                ${app.name}
            </g:else>
        </span>
    </td>
    <td id="health-${app.name}" class="has-text-centered status-column ">
        <a href="${app.healthUrl}"
           target="_blank"
           class="tag"
           style="line-height: 1.5rem"
           title="Health Check">-
        </a>
    </td>

    <td id="supervision-${app.name}" class="has-text-centered status-column ">
        <a href="${app.supervisionUrl}"
           target="_blank"
           class="tag"
           style="line-height: 1.5rem">
            supervision
        </a>
    </td>

    <td id="pipeline-tests-${app.name}" class="has-text-centered has-text-right status-column"
        data-gitlab-project="${app.id}">
        <a href="http://calculé-dans-le-js"
           target="_blank"
           class="pipeline-status-badge"
           style="line-height: 1.5rem">
            <i class="far fa-circle fa-xl"></i>
        </a>
    </td>
    <g:if test="${app.repositoryUrl}">
        <td class="icon-column has-text-centered has-text-right">

            <a href="${app.repositoryUrl}"
               target="_blank"
               class="icon is-medium has-text-dark"
               title="Repository">
                <i class="fab fa-github fa-lg"></i>
            </a>

        </td>
    </g:if>
    <g:if test="${app.logsUrl}">
        <td class="icon-column">

            <a href="${app.logsUrl}"
               target="_blank"
               class="icon is-medium has-text-dark"
               title="Logs">
                <i class="fas fa-file-alt fa-lg"></i>
            </a>

        </td>
    </g:if>
</tr>