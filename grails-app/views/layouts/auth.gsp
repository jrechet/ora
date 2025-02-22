<!DOCTYPE html>
<html>
<head>
    <title><g:layoutTitle default="Authentication"/></title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <asset:stylesheet src="vendor/fontawesome/css/all.min.css"/>
    <asset:stylesheet src="application.css"/>
    <asset:javascript src="application.js"/>
    <g:layoutHead/>
</head>
<body>
    <section class="section">
        <div class="container">
            <g:if test="${flash.message}">
                <div class="notification is-danger is-light">
                    <button class="delete"></button>
                    ${flash.message}
                </div>
            </g:if>
            <g:layoutBody/>
        </div>
    </section>
</body>
</html>