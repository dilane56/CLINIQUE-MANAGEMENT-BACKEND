#!/bin/bash
# Script de déploiement Railway

echo "🚀 Démarrage du déploiement Railway..."

# Variables d'environnement requises pour Railway
export SPRING_PROFILES_ACTIVE=prod
export JAVA_OPTS="-Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom"

# Vérifier que les variables critiques sont définies
if [ -z "$SPRING_DATASOURCE_URL" ]; then
    echo "❌ SPRING_DATASOURCE_URL non définie"
    exit 1
fi

if [ -z "$JWT_SECRET" ]; then
    echo "❌ JWT_SECRET non définie"
    exit 1
fi

echo "✅ Variables d'environnement vérifiées"

# Démarrer l'application
echo "🏃 Démarrage de l'application..."
java $JAVA_OPTS -Dspring.profiles.active=prod -Dserver.port=${PORT:-8080} -jar target/CLINIQUE-MANAGEMENT-BACKEND-0.0.1-SNAPSHOT.jar