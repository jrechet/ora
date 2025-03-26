<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="backoffice"/>
    <title>Tableau de bord - Backoffice</title>
    <content tag="pageTitle">Tableau de bord</content>
</head>
<body>
    <div class="dashboard-container">
        <div class="dashboard-row">
            <!-- Widget du système -->
            <div class="dashboard-widget">
                <div class="widget-header">
                    <h3>État du système</h3>
                </div>
                <div class="widget-content">
                    <p>Statut: <span class="status-badge success">En ligne</span></p>
                    <p>Dernier redémarrage: <span>01/01/2025 12:00</span></p>
                    <p>Ressources: <span>CPU: 15%, RAM: 30%</span></p>
                </div>
                <div class="widget-footer">
                    <g:link controller="systemInfo" action="index">Plus de détails</g:link>
                </div>
            </div>
            
            <!-- Widget des alertes récentes -->
            <div class="dashboard-widget">
                <div class="widget-header">
                    <h3>Alertes récentes</h3>
                </div>
                <div class="widget-content">
                    <div class="alert-list">
                        <div class="alert-item warning">
                            <span class="alert-time">Aujourd'hui 10:30</span>
                            <span class="alert-message">Utilisation CPU élevée</span>
                        </div>
                        <div class="alert-item error">
                            <span class="alert-time">Hier 15:45</span>
                            <span class="alert-message">Erreur de connexion à la base de données</span>
                        </div>
                        <div class="alert-item info">
                            <span class="alert-time">Hier 09:15</span>
                            <span class="alert-message">Mise à jour système effectuée</span>
                        </div>
                    </div>
                </div>
                <div class="widget-footer">
                    <g:link controller="alert" action="list">Voir toutes les alertes</g:link>
                </div>
            </div>
        </div>
        
        <div class="dashboard-row">
            <!-- Widget des préférences d'alerte -->
            <div class="dashboard-widget">
                <div class="widget-header">
                    <h3>Préférences d'alerte</h3>
                </div>
                <div class="widget-content">
                    <p>Email: <span class="status-badge"><g:if test="${activePreference?.emailEnabled}">Activé</g:if><g:else>Désactivé</g:else></span></p>
                    <p>Navigateur: <span class="status-badge"><g:if test="${activePreference?.browserEnabled}">Activé</g:if><g:else>Désactivé</g:else></span></p>
                    <p>Système: <span class="status-badge"><g:if test="${activePreference?.systemEnabled}">Activé</g:if><g:else>Désactivé</g:else></span></p>
                </div>
                <div class="widget-footer">
                    <g:link controller="alertPreference" action="edit">Modifier les préférences</g:link>
                </div>
            </div>
            
            <!-- Widget des statistiques -->
            <div class="dashboard-widget">
                <div class="widget-header">
                    <h3>Statistiques système</h3>
                </div>
                <div class="widget-content">
                    <p>Uptime: <span>99.8%</span></p>
                    <p>Alertes ce mois: <span>12</span></p>
                    <p>Temps de réponse moyen: <span>124ms</span></p>
                </div>
                <div class="widget-footer">
                    <g:link controller="statistics" action="index">Voir toutes les statistiques</g:link>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
