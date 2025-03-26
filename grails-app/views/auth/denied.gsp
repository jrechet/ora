<!doctype html>
<html>
<head>
    <meta name="layout" content="monitoring"/>
    <title>Access Denied</title>
</head>
<body>
    <div class="columns is-centered">
        <div class="column is-half">
            <div class="box">
                <h3 class="title is-3 has-text-centered has-text-danger">
                    <span class="icon">
                        <i class="fas fa-exclamation-triangle"></i>
                    </span>
                    <span>Access Denied</span>
                </h3>

                <div class="notification is-danger is-light">
                    <p class="has-text-centered">Sorry, you're not authorized to view this page.</p>
                </div>

                <div class="has-text-centered">
                    <g:link controller="monitoring" action="index" class="button is-primary is-outlined">
                        <span class="icon">
                            <i class="fas fa-home"></i>
                        </span>
                        <span>Back to Home</span>
                    </g:link>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
