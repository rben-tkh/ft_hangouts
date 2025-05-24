#!/bin/bash
echo "=== Diagnostic pour ft_hangouts ==="
echo "Date: $(date)"
echo "Répertoire courant: $(pwd)"
echo "Utilisateur: $(whoami)"

echo -e "\n=== Structure du projet ==="
find . -type f -name "*.java" | sort

echo -e "\n=== Permissions des fichiers de build ==="
ls -la *.gradle gradlew

echo -e "\n=== Essai d'exécution avec --stacktrace ==="
./gradlew tasks --stacktrace

echo -e "\n=== Essai d'exécution du build ==="
./gradlew build --debug

echo -e "\n=== Fin du diagnostic ==="