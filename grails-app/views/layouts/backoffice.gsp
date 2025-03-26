<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:layoutTitle default="Backoffice"/></title>
    <asset:stylesheet src="backoffice.css"/>
    <g:layoutHead/>
    <%@ page import="java.time.LocalDateTime" %>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <asset:stylesheet src="vendor/fontawesome/css/all.min.css"/>
    <asset:stylesheet src="application.css"/>
    <asset:javascript src="application.js"/>
</head>

<body data-initial-mode="${currentMode}" data-refresh-interval="${refreshInterval}">
<g:render template="/layouts/navbar"/>

<div class="container is-fluid">

    <div class="columns is-gapless">
    <!-- Menu latéral -->
    <div class="column is-2 sidebar">
        <g:render template="/backoffice/sideMenu" model="[currentSection: currentSection ?: 'alertPreferences']"/>
    </div>

    <!-- Contenu principal -->
    <section class="section">
        <div class="container is-fluid">
            <g:if test="${flash.message}">
                <div class="notification is-info is-light">
                    <button class="delete"></button>
                    ${flash.message}
                </div>
            </g:if>
            <g:if test="${flash.error}">
                <div class="notification is-danger is-light">
                    <button class="delete"></button>
                    ${flash.error}
                </div>
            </g:if>

            <div class="content">
                <h1 class="title is-4"><g:pageProperty name="page.pageTitle" default="Backoffice"/></h1>
                <g:layoutBody/>
            </div>
        </div>
    </section>
</div>
</div>
<script>
    $(document).ready(function () {
        // Gestion de la déconnexion
        $('#logoutButton').click(function () {
            $.ajax({
                url: '/api/logout',
                type: 'POST',
                headers: {
                    'Authorization': 'Bearer ' + localStorage.getItem('access_token')
                },
                success: function () {
                    localStorage.removeItem('access_token');
                    window.location.href = '${createLink(controller: 'auth', action: 'login')}';
                },
                error: function () {
                    localStorage.removeItem('access_token');
                    window.location.href = '${createLink(controller: 'auth', action: 'login')}';
                }
            });
        });

        // Gestion des notifications
        $('.notification .delete').on('click', function () {
            $(this).parent().fadeOut(300, function () {
                $(this).remove();
            });
        });
    });
</script>
<asset:javascript src="backoffice.js"/>
</body>
</html>
