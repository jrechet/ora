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
<nav class="navbar" role="navigation" aria-label="main navigation">
    <div class="navbar-brand">
        <a class="navbar-item" href="${createLink(controller: 'monitoring')}" style="text-decoration: none">
            <span class="logo">ORA</span>
        </a>

        <a role="button" class="navbar-burger" aria-label="menu" aria-expanded="false" data-target="navMenu">
            <span aria-hidden="true"></span>
            <span aria-hidden="true"></span>
            <span aria-hidden="true"></span>
        </a>
    </div>

    <div id="navMenu" class="navbar-menu">
        <div class="navbar-start">
            <a class="navbar-item" href="${createLink(controller: 'monitoring')}">
                <span class="icon-text">
                    <span>Healthcheck</span>
                </span>
            </a>
        </div>

        <div class="navbar-end">
            <div class="navbar-item">
                <div class="theme-switch-wrapper">
                    <label class="theme-switch" for="checkbox">
                        <input type="checkbox" id="checkbox"/>

                        <div class="slider"/>
                    </label>
                </div>
            </div>

            <div id="server-alert" class="navbar-item has-text-danger is-hidden">
                <span class="icon">
                    <i class="fas fa-exclamation-triangle"></i>
                </span>
                <span>Connection lost</span>
            </div>

            <div class="navbar-item">
                <div class="mode-toggle">
                    <div class="mode-option"
                         data-mode="websocket"
                         title="'WebSocket Not Available'">
                        <span class="icon">
                            <i class="fas fa-bolt"></i>
                        </span>
                        <span>WS</span>
                    </div>

                    <div class="mode-option ${serverToServerAvailable ? '' : 'disabled-mode'}"
                         data-mode="server"
                         title="${serverToServerAvailable ? 'Server to Server Mode' : 'Server Mode Not Available'}">
                        <span class="icon">
                            <i class="fas fa-server"></i>
                        </span>
                        <span>API</span>
                    </div>

                    <div class="mode-option"
                         data-mode="autonomous">
                        <span class="icon">
                            <i class="fas fa-code"></i>
                        </span>
                        <span>JS</span>
                    </div>

                    <div class="mode-slider"></div>
                </div>
            </div>

            <div class="navbar-item">
                <button id="refreshAll" class="button is-ghost">
                    <span class="icon">
                        <i class="fas fa-sync-alt"></i>
                    </span>
                    <span>Refresh All</span>
                </button>
            </div>
        </div>
    </div>
</nav>

<section class="section">
    <div class="container is-fluid">
        <g:if test="${flash.message}">
            <div class="notification is-info is-light">
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
</body>
</html>