<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="monitoring"/>
    <title>ORA - Login</title>
    <asset:javascript src="jquery-3.7.1.min.js"/>
</head>
<body>
    <div class="section">
        <div class="columns is-centered">
            <div class="column is-one-third">
                <div class="box">
                    <h3 class="title is-3 has-text-centered">Connexion</h3>
                    <g:if test='${flash.message}'>
                        <div class="notification is-danger">
                            <button class="delete"></button>
                            ${flash.message}
                        </div>
                    </g:if>
                    <form action="${postUrl ?: '/j_spring_security_check'}" method="POST" id="loginForm" autocomplete="off">
                        <div class="field">
                            <label class="label" for="username">Nom d'utilisateur</label>
                            <div class="control has-icons-left">
                                <input type="text" class="input" id="username" name="j_username" required autocapitalize="none">
                                <span class="icon is-small is-left">
                                    <i class="fas fa-user"></i>
                                </span>
                            </div>
                        </div>
                        <div class="field">
                            <label class="label" for="password">Mot de passe</label>
                            <div class="control has-icons-left">
                                <input type="password" class="input" id="password" name="j_password" required>
                                <span class="icon is-small is-left">
                                    <i class="fas fa-lock"></i>
                                </span>
                            </div>
                        </div>
                        <div class="field">
                            <div class="control">
                                <label class="checkbox">
                                    <input type="checkbox" name="${rememberMeParameter ?: 'remember-me'}" id="remember_me" <g:if test='${hasCookie}'>checked="checked"</g:if>/>
                                    Se souvenir de moi
                                </label>
                            </div>
                        </div>
                        <div class="field">
                            <div class="control has-text-centered">
                                <button type="submit" class="button is-primary is-outlined">
                                    <span class="icon">
                                        <i class="fas fa-sign-in-alt"></i>
                                    </span>
                                    <span>Se connecter</span>
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <script>
        // Handle notification close button
        document.addEventListener('DOMContentLoaded', () => {
            (document.querySelectorAll('.notification .delete') || []).forEach(($delete) => {
                const $notification = $delete.parentNode;
                $delete.addEventListener('click', () => {
                    $notification.parentNode.removeChild($notification);
                });
            });
        });
    </script>
</body>
</html>
