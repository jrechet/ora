<!doctype html>
<html>
<head>
    <meta name="layout" content="auth"/>
    <title>Login</title>
</head>
<body>
    <div class="columns is-centered">
        <div class="column is-half">
            <div class="box">
                <h3 class="title is-3 has-text-centered">Please Login</h3>

                <form action="${postUrl ?: '/j_spring_security_check'}" method="POST" id="loginForm" autocomplete="off">
                    <div class="field">
                        <label class="label" for="username">Username</label>
                        <div class="control has-icons-left">
                            <input type="text" class="input" name="j_username" id="username" autocapitalize="none"/>
                            <span class="icon is-small is-left">
                                <i class="fas fa-user"></i>
                            </span>
                        </div>
                    </div>

                    <div class="field">
                        <label class="label" for="password">Password</label>
                        <div class="control has-icons-left">
                            <input type="password" class="input" name="j_password" id="password"/>
                            <span class="icon is-small is-left">
                                <i class="fas fa-lock"></i>
                            </span>
                        </div>
                    </div>

                    <div class="field">
                        <div class="control">
                            <label class="checkbox">
                                <input type="checkbox" name="${rememberMeParameter ?: 'remember-me'}" id="remember_me" <g:if test='${hasCookie}'>checked="checked"</g:if>/>
                                Remember me
                            </label>
                        </div>
                    </div>

                    <div class="field">
                        <div class="control">
                            <button class="button is-primary is-outlined is-fullwidth" type="submit">
                                <span class="icon">
                                    <i class="fas fa-sign-in-alt"></i>
                                </span>
                                <span>Login</span>
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</body>
</html>
