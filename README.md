# Guide d'utilisation Docker pour ORA

## Introduction

ORA est distribué sous forme d'image Docker pour faciliter son déploiement. Cette documentation explique comment
utiliser l'image Docker et configurer ORA avec vos propres fichiers de configuration YAML.

## Configuration requise

- Docker installé sur votre machine
- Vos fichiers de configuration YAML prêts à l'emploi

## Structure des fichiers de configuration

Vos fichiers de configuration doivent suivre cette structure :

```
ora/
├── apps/
│   ├── app1.yml
│   ├── app2.yml
│   └── ...
└── envs.yml
```

Un exemple complet est disponible dans le répertoire `grails-app/simulator/config/local`.

envs.yml contient la liste des environnements et leurs propriétés :

```yaml
# envs.yml
environments:
   development: # environment au sens grails
      applications:
         monitoring:
            envs:
               dev:
                  params:
                     level: 1
                  tenants:
                     tenant-a:
                     tenant-b:
               prod:
                  params:
                     level: 99
                  tenants:
                     tenant-a:
                     tenant-b:
   production: # environment au sens grails
      applications:
         monitoring:
            envs:
               dev:
                  params:
                     level: 1
                  tenants:
                     tenant-a:
                     tenant-b:
               prod:
                  params:
                     level: 99
                  tenants:
                     tenant-a:
                     tenant-b:

```

le repertoire `apps` contient les fichiers de configuration des applications.

```yaml
# app-1.yml
environments:
  development:
    applications:
      monitoring:
        apps:
          test-app1:
            global:
              repositoryUrl: "http://app1:3000"
              healthUrl: "/healthcheck"
              supervisionUrl: "/supervision"
            instances:
              dev:
                tenant-a:
                  baseUrl: "http://localhost:3001"
                tenant-b:
                  baseUrl: "http://localhost:3002"
              prod:
                tenant-a:
                  baseUrl: "http://localhost:3003"
```

On peut créer un fichier de configuration pour chaque application ou un seul fichier contenant la configuration de
toutes les applications.

## Développement

### Variables d'environnement

- `ORA_CONFIG_PATH` : Chemin vers le répertoire contenant les configurations
- `GITLAB_TOKEN` est le token d'accès à l'API Gitlab permettant de récupérer les informations des pipelines.

### Gadle

#### Construction du livrable :

```bash
./gradlew assemble
```

#### Execution

```bash
./gradlew bootRun -Dgrails.env=prod -Dserver.port=8080 -Dora.config.path=./grails-app/conf/ora -Dgitlab.token=xxxxx
```

Pensez à remplacer `xxxxx` par votre token Gitlab.

### Docker

#### Récupération de l'image docker

```bash
docker pull jrec/ora:latest
```

#### Exécution

```bash
docker run --name ora -p 8080:8080 -e CONFIG_PATH=./grails-app/conf/ora -e GITLAB_TOKEN=xxxxx -t jrec/ora:latest
```

pensez à remplacer `xxxxx` par votre token Gitlab.

#### Logs

Pour visualiser les logs :

```bash
docker logs ora
```

Pour suivre les logs en temps réel :

```bash
docker logs -f ora
```

#### État du conteneur

Pour vérifier l'état du conteneur :

```bash
docker ps -f name=ora
```

#### Redémarrage

Pour redémarrer le conteneur :

```bash
docker restart ora
```

## Dépannage

### Problèmes courants

1. **Les configurations ne sont pas chargées**
    - Vérifiez le chemin de montage
    - Vérifiez les permissions des fichiers
    - Consultez les logs avec `docker logs ora`

2. **Erreur de mémoire**
    - Ajustez les paramètres JVM via JAVA_OPTS
    - Vérifiez les ressources disponibles pour Docker

3. **Port déjà utilisé**
    - Changez le port de mapping :
      ```bash
      docker run -p 8081:8080 ...
      ```