# CLINIQUE MANAGEMENT BACKEND

API REST pour la gestion d'une clinique médicale développée avec Spring Boot.

## 🚀 Technologies
- Java 17
- Spring Boot 3.4.5
- Spring Security
- PostgreSQL
- JWT Authentication
- WebSocket
- iText PDF
- Maven

## 📋 Configuration

### Base de données
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/cliniquedb
spring.datasource.username=postgres
spring.datasource.password=12345678
```

### Serveur
- Port: 9001
- Swagger UI: http://localhost:9001/swagger-ui.html

## 🔐 Authentification

### POST /api/auth/login
Connexion utilisateur
```json
{
  "email": "user@example.com",
  "motDePasse": "password"
}
```
Default user:
```json
{
  "email": "admin@gmail.com",
  "password": "password"
}
```
**Réponse:**
```json
{
  "token": "jwt_token",
  "user": {...}
}
```

## 👥 Gestion des Utilisateurs

### Patients

#### POST /api/patients
**Rôles:** ADMIN, SECRETAIRE  
Créer un nouveau patient

#### GET /api/patients
**Rôles:** MEDECIN, ADMIN, SECRETAIRE  
Lister tous les patients

#### GET /api/patients/{id}
**Rôles:** MEDECIN, ADMIN, SECRETAIRE  
Récupérer un patient par ID

#### GET /api/patients/medecin/{medecinId}
**Rôles:** MEDECIN, ADMIN, SECRETAIRE  
Lister les patients d'un médecin

#### PUT /api/patients/{id}
**Rôles:** MEDECIN, ADMIN, SECRETAIRE, PATIENT  
Mettre à jour un patient

#### DELETE /api/patients/{id}
**Rôles:** MEDECIN, ADMIN, SECRETAIRE  
Supprimer un patient

### Médecins

#### POST /api/medecins
**Rôles:** ADMIN  
Créer un nouveau médecin

#### GET /api/medecins
**Rôles:** ADMIN, SECRETAIRE  
Lister tous les médecins

#### GET /api/medecins/{id}
**Rôles:** MEDECIN, ADMIN, SECRETAIRE  
Récupérer un médecin par ID

#### PUT /api/medecins/{id}
**Rôles:** MEDECIN, ADMIN  
Mettre à jour un médecin

#### DELETE /api/medecins/{id}
**Rôles:** ADMIN  
Supprimer un médecin

## 📅 Gestion des Rendez-vous

#### POST /api/rendezvous
Créer un nouveau rendez-vous

#### GET /api/rendezvous
Lister tous les rendez-vous

#### GET /api/rendezvous/{id}
Récupérer un rendez-vous par ID

#### PUT /api/rendezvous/{id}
Mettre à jour un rendez-vous

#### DELETE /api/rendezvous/{id}
Supprimer un rendez-vous

#### GET /api/rendezvous/medecin/{medecinId}
Lister les rendez-vous d'un médecin

#### PATCH /api/rendezvous/{id}/statut?statut={STATUT}
Mettre à jour le statut d'un rendez-vous

#### GET /api/rendezvous/medecin/{medecinId}/aujourd'hui
Récupérer les rendez-vous du jour pour un médecin

## 💰 Gestion des Factures

#### POST /api/factures
Créer une nouvelle facture

#### GET /api/factures
Lister toutes les factures

#### GET /api/factures/{id}
Récupérer une facture par ID

#### GET /api/factures/medecin/{medecinId}
Lister les factures d'un médecin

#### PUT /api/factures/{id}
Mettre à jour une facture

#### PUT /api/factures/{id}/paiement
Mettre à jour le paiement d'une facture

#### DELETE /api/factures/{id}
Supprimer une facture

#### GET /api/factures/{id}/pdf
Générer et télécharger le PDF d'une facture

## 📊 Statistiques

#### GET /api/revenus
Récupérer les statistiques de revenus
**Réponse:**
```json
{
  "revenuMensuel": 125000.50,
  "revenuMoisPrecedent": 115000.75,
  "pourcentageEvolution": 8.2
}
```

## 🔔 Notifications

#### GET /api/notifications
Lister les notifications

#### POST /api/notifications
Créer une notification

## 💬 Messagerie (WebSocket)

#### WebSocket: /ws
Connexion WebSocket pour la messagerie en temps réel

#### POST /api/messages
Envoyer un message

#### GET /api/messages
Récupérer les messages

## 📧 Email (Test)

#### POST /api/test/send-email
Tester l'envoi d'email
```json
{
  "destinataireEmail": "test@example.com",
  "sujet": "Test",
  "message": "Message de test"
}
```

## 🏃‍♂️ Démarrage

1. Cloner le repository
2. Configurer PostgreSQL
3. Définir la variable d'environnement `mailpass` pour Gmail
4. Exécuter:
```bash
mvn spring-boot:run
```

## 📦 Déploiement sur Render.com

Voici une procédure pas-à-pas pour déployer ce backend Spring Boot sur Render (option native ou Docker).

Option A — Web Service (build natif Render)

- Build Command : `mvn -DskipTests package`
- Start Command : `java $JAVA_OPTS -jar target/CLINIQUE-MANAGEMENT-BACKEND-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod --server.port=$PORT`
- Variables d'environnement à définir dans Render (remplacez les placeholders) :
  - `SPRING_DATASOURCE_URL` = `jdbc:postgresql://<DB_HOST>:5432/<DB_NAME>`
  - `SPRING_DATASOURCE_USERNAME` = `<DB_USER>`
  - `SPRING_DATASOURCE_PASSWORD` = `<DB_PASS>`
  - `MAIL_USERNAME` = `<MAIL_USER>`
  - `MAIL_PASSWORD` = `<MAIL_PASS>`
  - `JWT_SECRET` = `<JWT_SECRET>`
  - `JAVA_OPTS` = `-Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom` (optionnel)

Notes : Render fournit la variable `$PORT` au runtime ; l'application lit `server.port=${PORT:9001}` depuis `application-prod.properties`.

Option B — Docker (recommandé si vous voulez contrôler l'image)

- Un `Dockerfile` multi-stage est fourni à la racine du projet.
- `.dockerignore` est aussi fourni.
- Pour builder et lancer localement :

```powershell
# build l'image
docker build -t clinique-backend:latest .

# run (expose port 8080 et passe les variables d'environnement)
docker run --rm -e SPRING_DATASOURCE_URL="jdbc:postgresql://<DB_HOST>:5432/<DB_NAME>" -e SPRING_DATASOURCE_USERNAME="<DB_USER>" -e SPRING_DATASOURCE_PASSWORD="<DB_PASS>" -e MAIL_USERNAME="<MAIL_USER>" -e MAIL_PASSWORD="<MAIL_PASS>" -e JWT_SECRET="<JWT_SECRET>" -e PORT=8080 -p 8080:8080 clinique-backend:latest
```

Render.yml (option Git deploy)

- Un exemple `render.yaml` est fourni. Remplacez les placeholders et poussez sur la branche configurée.

Checklist avant déploiement

- `mvn -DskipTests package` doit construire `target/CLINIQUE-MANAGEMENT-BACKEND-0.0.1-SNAPSHOT.jar` sans erreur.
- `src/main/resources/application-prod.properties` lit les variables d'environnement (déjà configuré).
- N'ajoutez jamais de secrets en clair dans le repo ; utilisez les Environment Variables de Render.
- (Optionnel) Ajoutez `spring-boot-starter-actuator` si vous voulez un endpoint `/actuator/health` pour les health checks.

Tests locaux rapides (PowerShell)

- Builder et exécuter le jar localement (profil prod) :

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://<DB_HOST>:5432/<DB_NAME>"; $env:SPRING_DATASOURCE_USERNAME="<DB_USER>"; $env:SPRING_DATASOURCE_PASSWORD="<DB_PASS>"; $env:MAIL_USERNAME="<MAIL_USER>"; $env:MAIL_PASSWORD="<MAIL_PASS>"; $env:JWT_SECRET="<JWT_SECRET>"; mvn -DskipTests package; java -jar target/CLINIQUE-MANAGEMENT-BACKEND-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod --server.port=9001
```

Sécurité et recommandations post-déploiement

- Ne pas committer les secrets.
- Activer backups pour la base de données managée.
- Forcer SSL si nécessaire (`?sslmode=require` dans `SPRING_DATASOURCE_URL`).
- Configurer health checks (Actuator recommandé) et surveiller les logs via Render.

Fichiers ajoutés

- `Dockerfile` (multi-stage)
- `.dockerignore`
- `render.yaml` (exemple)

## 📝 Notes
- Tous les endpoints nécessitent une authentification JWT (sauf /api/auth/login)
- Les rôles disponibles: ADMIN, MEDECIN, SECRETAIRE, PATIENT
- Documentation Swagger disponible sur /swagger-ui.html