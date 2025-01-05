# Simulateur de Services Mock

Un simulateur simple qui fournit des points d'accès (endpoints) fictifs pour tester des systèmes de monitoring et de contrôle d'état. Le simulateur comprend des endpoints pour les
contrôles de santé (healthcheck), la surveillance (monitoring) et les services de supervision.

## Fonctionnalités

- Endpoint de contrôle de santé (`/healthcheck`)
- Endpoint de monitoring (`/monitoring`)
- Endpoint de supervision (`/supervision`) avec des statuts de service dynamiques
- Mode statique (via variables d'environnement) ou dynamique (changements aléatoires toutes les 5 secondes)
- Instances de service multiples avec différentes configurations

## Comportement des Statuts

Le simulateur peut fonctionner selon deux modes pour chaque endpoint :

### Mode Statique

- Activé en définissant les variables d'environnement correspondantes
- Le statut reste constant à la valeur spécifiée
- Utile pour simuler des états spécifiques et stables

### Mode Dynamique (par défaut)

- Activé quand les variables d'environnement ne sont pas définies
- Les statuts changent aléatoirement toutes les 5 secondes
- 70% de chance d'être OK (200), 30% de chance d'être KO (500)

## Points d'Accès

### Contrôle de Santé

- Chemin : `/healthcheck`
- Retourne : Réponse JSON avec statut et horodatage
- Exemple de réponse :

```json
{
  "status": "OK",
  "timestamp": "2024-12-11T10:00:00.000Z"
}
```

### Monitoring

- Chemin : `/monitoring`
- Retourne : Réponse JSON avec statut et horodatage
- Exemple de réponse :

```json
{
  "status": "OK",
  "timestamp": "2024-12-11T10:00:00.000Z"
}
```

### Supervision

- Chemin : `/supervision`
- Retourne : Table HTML avec le statut de différents services
- Inclut les statuts pour :
    - Connexion à la Base de Données
    - Service de Cache
    - Service d'Authentification
    - Service Email

## Variables d'Environnement

Chaque service peut être configuré avec :

- `HEALTH_STATUS` : Code de statut pour l'endpoint de santé
    - Si non défini : change aléatoirement
    - Si défini : reste fixe à la valeur spécifiée (ex: 200 ou 500)
- `MONITORING_STATUS` : Code de statut pour l'endpoint de monitoring
    - Même comportement que HEALTH_STATUS
- `SUPERVISION_STATUS` : Code de statut pour l'endpoint de supervision
    - Même comportement que HEALTH_STATUS

### Exemple de Configuration dans docker-compose.yml

```yaml
services:
  app1:
    environment:
      - HEALTH_STATUS=200    # Toujours OK
      - MONITORING_STATUS=500  # Toujours en erreur
      # Supervision en mode dynamique car non défini
```

## Construction de l'Image Docker

```bash
docker build -t mock-services .
```

## Lancement avec Docker Compose

Le fichier docker-compose.yml fourni inclut 5 instances de service avec différentes configurations :

```bash
docker compose up -d
```

### Instances de Service

- **app1** : (Port 3001)
    - Tous les services retournent un statut 200 (fixe)
- **app2** : (Port 3002)
    - Le healthcheck retourne 500 (fixe)
    - Les autres services retournent 200 (fixe)
- **app3** : (Port 3003)
    - Le monitoring retourne 500 (fixe)
    - Les autres services retournent 200 (fixe)
- **app4** : (Port 3004)
    - La supervision retourne 500 (fixe)
    - Les autres services retournent 200 (fixe)
- **app5** : (Port 3005)
    - Tous les services retournent 500 (fixe)

### Arrêt des Services

```bash
docker compose down
```

## Développement

### Prérequis

- Node.js 18
- Docker
- Docker Compose

### Lancement en Local

```bash
npm install
npm start
```

Le service démarrera par défaut sur le port 3000. Vous pouvez modifier cela en utilisant la variable d'environnement `PORT`.