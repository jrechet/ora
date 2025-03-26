<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="backoffice"/>
    <title>Préférences d'alerte</title>
    <content tag="pageTitle">Préférences d'alerte</content>
</head>

<body>
<div class="box">
    <g:form controller="alertPreference" action="update" method="POST">
        <g:hiddenField name="id" value="${preference?.id}"/>

        <div class="field">
            <label class="label">Préférences d'alerte</label>

            <p class="help">Personnalisez vos préférences pour recevoir des alertes en cas d'incident.</p>
        </div>

        <g:render template="/components/toggleSwitch"
                  model="[name   : 'browserEnabled',
                          checked: preference?.browserEnabled,
                          label  : 'Notifications navigateur']"/>

        <g:render template="/components/toggleSwitch"
                  model="[name   : 'systemEnabled',
                          checked: preference?.systemEnabled,
                          label  : 'Notifications système']"/>

        <g:render template="/components/toggleSwitch"
                  model="[name   : 'emailEnabled',
                          checked: preference?.emailEnabled,
                          label  : 'Notifications par email']"/>

        <div class="field">
            <label class="label">Destinataires des emails</label>

            <div class="control">
                <g:textArea name="emailRecipients"
                            value="${preference?.emailRecipients}"
                            class="textarea"
                            placeholder="Exemple: admin@example.com, support@example.com"/>
            </div>

            <p class="help">Séparez les adresses email par des virgules.</p>
        </div>

        <div class="field is-grouped mt-5">
            <div class="control">
                <g:submitButton name="saveButton" value="Enregistrer" class="button is-primary is-outlined"/>
            </div>

            <div class="control">
                <g:link controller="backoffice" action="index" class="button is-light is-outlined">Annuler</g:link>
            </div>
        </div>
    </g:form>
</div>
</body>
</html>
