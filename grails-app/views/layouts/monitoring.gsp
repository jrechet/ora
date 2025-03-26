<%@ page import="java.time.LocalDateTime" %>
<%-- grails-app/views/layouts/monitoring.gsp --%>
<!DOCTYPE html>
<html>
<head>
    <title>ORA</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <!-- Font Awesome local -->
    <asset:stylesheet src="vendor/fontawesome/css/all.min.css"/>

    <asset:stylesheet src="application.css"/>
    <asset:javascript src="application.js"/>

    <g:layoutHead/>
</head>


<body data-initial-mode="${currentMode}" data-refresh-interval="${refreshInterval}">
<g:render template="/layouts/navbar"/>

<section class="section">
    <div class="container is-fluid">
        <g:if test="${flash.message}">
            <div class="notification is-info is-light is-outlined">
                <button class="delete"></button>
                ${flash.message}
            </div>
        </g:if>

        <g:layoutBody/>
    </div>
</section>

<footer class="footer py-2">
    <div class="content">
        <g:render template="/monitoring/summary" model="[summary: summary]"/>
    </div>
</footer>

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
    });
</script>
</body>
</html>