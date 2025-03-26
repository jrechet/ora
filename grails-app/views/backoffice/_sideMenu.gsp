<!-- Menu latéral du backoffice avec Bulma.css -->
<aside class="menu p-4">
    <p class="menu-label">
        Ora Backoffice
    </p>
    <ul class="menu-list">
        <li>
            <g:link controller="alertPreference" action="index" class="${currentSection == 'alertPreferences' ? 'is-active' : ''}">
                <span class="icon-text">
                    <span class="icon">
                        <i class="fas fa-bell"></i>
                    </span>
                    <span>Préférences d'alerte</span>
                </span>
            </g:link>
        </li>
    </ul>
    
    <div class="menu-footer mt-5">
        <div class="user-info has-text-centered">
            <sec:ifLoggedIn>
                <p class="is-size-7 has-text-grey mb-2">
                    Connecté en tant que:
                </p>
                <p class="has-text-weight-bold">
                    <sec:username/>
                </p>
            </sec:ifLoggedIn>
        </div>
    </div>
</aside>
