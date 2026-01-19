# Guide de Déploiement - Railway & Render

## 🚂 Déploiement sur Railway

### Profil utilisé : `railway`
### Fichier de config : `application-railway.properties`

### Variables d'environnement Railway :
```bash
# Base de données (format JDBC)
SPRING_DATASOURCE_URL=jdbc:postgresql://host:port/database
SPRING_DATASOURCE_USERNAME=username
SPRING_DATASOURCE_PASSWORD=password

# JWT
JWT_SECRET=your-super-secret-jwt-key

# Email
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

---

## 🎨 Déploiement sur Render

### Profil utilisé : `render`
### Fichier de config : `application-render.properties`

### Variables d'environnement Render :
```bash
# Base de données (format PostgreSQL - Render fournit DATABASE_URL)
DATABASE_URL=postgresql://user:pass@host:port/database

# JWT
JWT_SECRET=your-super-secret-jwt-key

# Email
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

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