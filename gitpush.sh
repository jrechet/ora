#!/bin/bash

# Obtenir la branche courante
CURRENT_BRANCH=$(git symbolic-ref --short HEAD)

# Sauvegarder le contenu du fichier .gitlab-ci.yml s'il existe
if [ -f ".gitlab-ci.yml" ]; then
    mv .gitlab-ci.yml .gitlab-ci.yml.backup
fi

# Push vers GitHub avec la branche courante
git push dist $CURRENT_BRANCH

# Restaurer le fichier .gitlab-ci.yml
if [ -f ".gitlab-ci.yml.backup" ]; then
    mv .gitlab-ci.yml.backup .gitlab-ci.yml
fi