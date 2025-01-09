<%-- _tenant.gsp --%>
<div id="tab-${tenant}" class="tab-content ${active ? 'is-active' : ''}">
    <div class="columns is-multiline is-centered">
        <g:each in="${environments}" var="env">
            <div class="column is-narrow">
                <g:render template="/monitoring/card"
                          model="[
                                  environment: env.key,
                                  applications: env.value
                          ]"/>
            </div>
        </g:each>
    </div>
</div>