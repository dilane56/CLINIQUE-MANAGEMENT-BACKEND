# Multi-stage Dockerfile pour Spring Boot - Render
# Build stage
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app

# Copier les fichiers de configuration Maven
COPY pom.xml ./
COPY .mvn/ .mvn/
COPY mvnw ./

# Télécharger les dépendances (cache layer)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copier le code source
COPY src/ ./src/

# Builder l'application
RUN ./mvnw -B -DskipTests clean package

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
ARG PORT=10000
ENV PORT=${PORT}
EXPOSE ${PORT}

# Créer un utilisateur non-root pour la sécurité
RUN addgroup -g 1001 -S spring && adduser -u 1001 -S spring -G spring
# Créer dossier de logs et donner la propriété à l'utilisateur spring
RUN mkdir -p /tmp/logs && chown -R 1001:1001 /tmp/logs
USER spring:spring

# Copier le jar produit
COPY --from=build --chown=spring:spring /app/target/*.jar /app/app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT}/health || exit 1

# Démarrer l'application avec le profil render
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=render -Dserver.port=${PORT} -jar /app/app.jar"]

