# Multi-stage Dockerfile pour Spring Boot (Maven wrapper)
# Build stage
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app

# Copier les fichiers nécessaires pour le build (inclure mvnw et .mvn si présents)
COPY pom.xml ./
COPY .mvn/ .mvn/
COPY mvnw ./
COPY src/ ./src/

# Rendre mvnw exécutable et builder
RUN chmod +x mvnw || true
RUN ./mvnw -B -DskipTests package

# Runtime stage
FROM eclipse-temurin:17-jre
ARG PORT=8080
ENV PORT=${PORT}
EXPOSE ${PORT}

# Copier le jar produit
COPY --from=build /app/target/*.jar /app/app.jar

# Démarrer l'application en respectant la variable PORT fournie par Render
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar /app/app.jar"]

