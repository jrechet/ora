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


    <sec:ifLoggedIn>
        <div id="navMenu" class="navbar-menu">
            <div class="navbar-start">
                <a class="navbar-item" href="${createLink(controller: 'monitoring')}">
                    <span class="icon-text">
                        <span>Healthcheck</span>
                    </span>
                </a>

                <a class="navbar-item" href="${createLink(controller: 'alertPreference')}">
                    <span class="icon-text">
                        <span>Backoffice</span>
                    </span>
                </a>
            </div>

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

            <div class="navbar-item">
                <button id="logoutButton" class="button is-light is-outlined">
                    <span class="icon">
                        <i class="fas fa-sign-out-alt"></i>
                    </span>
                    <span>Déconnexion</span>
                </button>
            </div>
        </div>
    </sec:ifLoggedIn>

</nav>