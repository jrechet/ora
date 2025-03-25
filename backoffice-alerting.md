# Backoffice Alerting - Liste des tâches de développement

## 1. Domain Classes + Tests Unitaires

### 1.1 Domain Classes
- [x] Créer `AlertPreference` domain class
  - Champs: `boolean emailEnabled`, `boolean browserEnabled`, `boolean systemEnabled`
  - Associer à l'utilisateur (User) si un modèle d'utilisateur existe, sinon créer une préférence globale
  - Ajouter contraintes de validation

### 1.2 Tests Unitaires (Domain)
- [x] Tester les contraintes de validation pour `AlertPreference`
- [x] Tester la persistence des données
- [x] Tester les associations (si applicable)

## 2. Services + Tests Unitaires

### 2.1 Services
- [x] Créer `AlertPreferenceService`
  - Méthodes pour récupérer les préférences
  - Méthodes pour sauvegarder les préférences
  - Méthodes pour vérifier les préférences actives lors d'une alerte

### 2.2 Services de Notification
- [x] Créer/Modifier `EmailAlertService` (pour les alertes par email)
- [x] Créer `BrowserAlertService` (pour les alertes navigateur)
- [x] Créer `SystemAlertService` (pour les alertes système)

### 2.3 Tests Unitaires (Services)
- [x] Tester `AlertPreferenceService`
- [x] Tester `EmailAlertService`
- [x] Tester `BrowserAlertService`
- [x] Tester `SystemAlertService`
- [x] Tester les cas d'erreurs et les cas limites

## 3. Controllers

### 3.1 Controllers
- [ ] Créer `BackofficeController`
  - Action par défaut pour afficher la page d'accueil du backoffice
- [ ] Créer `AlertPreferenceController`
  - Action pour afficher le formulaire des préférences d'alerte
  - Action pour sauvegarder les préférences d'alerte

## 4. Views et Templates

### 4.1 Layout Template Backoffice
- [ ] Créer `grails-app/views/layouts/backoffice.gsp`
  - Implémenter le menu latéral fixe
  - Intégrer les styles nécessaires
  - Définir la structure de base des pages backoffice

### 4.2 Pages et Partials
- [ ] Créer `grails-app/views/backoffice/index.gsp` (page d'accueil du backoffice)
- [ ] Créer `grails-app/views/backoffice/_sideMenu.gsp` (template du menu latéral)
- [ ] Créer `grails-app/views/alertPreference/edit.gsp` (formulaire des préférences d'alerte)
  - Intégrer les toggles pour Email, Navigateur et Système

### 4.3 Assets (CSS/JS)
- [ ] Créer/Modifier les fichiers CSS pour le layout backoffice
- [ ] Créer/Modifier les fichiers JS pour la gestion des toggles

## 5. Navigation et Intégration

### 5.1 Ajout du lien dans la barre de navigation
- [ ] Modifier `grails-app/views/layouts/monitoring.gsp`
  - Ajouter un lien "Backoffice" dans la barre de navigation

### 5.2 Configuration des routes
- [ ] Mettre à jour UrlMappings.groovy pour les nouvelles routes backoffice

## 6. Tests d'Intégration
- [ ] Tester la navigation entre pages
- [ ] Tester la sauvegarde des préférences
- [ ] Tester le déclenchement des alertes selon les préférences

## 7. Documentation
- [ ] Documenter les nouvelles fonctionnalités
- [ ] Mettre à jour la documentation utilisateur
