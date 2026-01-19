# Guide de Déploiement - Railway & Render

## 🚂 Déploiement sur Railway

### 1. Prérequis
- Compte Railway
- Repository Git public/privé
- Base de données PostgreSQL (Railway Postgres ou externe)

### 2. Variables d'environnement à configurer
```bash
# Base de données (OBLIGATOIRE)
SPRING_DATASOURCE_URL=postgresql://user:password@host:port/database
SPRING_DATASOURCE_USERNAME=your_db_user
SPRING_DATASOURCE_PASSWORD=your_db_password

# JWT (OBLIGATOIRE)
JWT_SECRET=your-super-secret-jwt-key-minimum-256-bits

# Email (OBLIGATOIRE)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Optionnel
SPRING_PROFILES_ACTIVE=prod
JAVA_OPTS=-Xms256m -Xmx512m
```

### 3. Étapes de déploiement
1. Connecter votre repo à Railway
2. Railway détectera automatiquement le `nixpacks.toml`
3. Configurer les variables d'environnement
4. Déployer

### 4. Health Check
- URL: `https://your-app.railway.app/actuator/health`
- Railway vérifiera automatiquement cette URL

---

## 🎨 Déploiement sur Render

### 1. Prérequis
- Compte Render
- Repository Git public
- Base de données PostgreSQL (Render Postgres ou externe)

### 2. Options de déploiement

#### Option A: Web Service (Dockerfile)
1. Créer un nouveau Web Service
2. Connecter votre repository
3. Choisir "Docker" comme environnement
4. Dockerfile path: `Dockerfile`

#### Option B: Utiliser render.yaml
1. Pousser le fichier `render.yaml` dans votre repo
2. Render détectera automatiquement la configuration

### 3. Variables d'environnement Render
```bash
# Base de données
SPRING_DATASOURCE_URL=postgresql://user:password@host:port/database
SPRING_DATASOURCE_USERNAME=your_db_user
SPRING_DATASOURCE_PASSWORD=your_db_password

# JWT
JWT_SECRET=your-super-secret-jwt-key

# Email
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Render spécifique
PORT=10000  # Render fournit automatiquement
```

### 4. Health Check Render
- URL: `https://your-app.onrender.com/actuator/health`
- Timeout: 300 secondes (configuré dans render.yaml)

---

## 🔧 Résolution de problèmes

### Erreurs communes

#### 1. "Application failed to start"
- Vérifier les variables d'environnement
- Vérifier les logs: `railway logs` ou Render dashboard

#### 2. "Database connection failed"
- Vérifier SPRING_DATASOURCE_URL
- Vérifier que la DB accepte les connexions externes
- Vérifier les credentials

#### 3. "Health check failed"
- Vérifier que `/actuator/health` répond
- Augmenter le timeout si nécessaire

#### 4. "Build failed"
- Vérifier que `mvnw` est exécutable
- Vérifier les dépendances dans `pom.xml`

### Commandes utiles

#### Railway
```bash
# Installer Railway CLI
npm install -g @railway/cli

# Login
railway login

# Voir les logs
railway logs

# Variables d'environnement
railway variables
```

#### Render
- Utiliser le dashboard web
- Logs disponibles dans l'interface

---

## 📊 Monitoring

### Endpoints disponibles après déploiement
- API: `https://your-app/api/`
- Health: `https://your-app/actuator/health`
- Swagger: `https://your-app/swagger-ui.html`

### Métriques
- Railway: Dashboard intégré
- Render: Dashboard intégré + logs

---

## 🔒 Sécurité

### Variables sensibles à ne JAMAIS committer
- `JWT_SECRET`
- `SPRING_DATASOURCE_PASSWORD`
- `MAIL_PASSWORD`
- Toute clé API

### Bonnes pratiques
- Utiliser des secrets forts (256+ bits pour JWT)
- Activer SSL en production
- Limiter l'exposition des endpoints Actuator
- Surveiller les logs pour les tentatives d'intrusion