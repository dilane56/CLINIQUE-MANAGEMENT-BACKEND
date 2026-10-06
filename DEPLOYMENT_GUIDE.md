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

# Administrateur initial (créé au premier démarrage)
DEFAULT_ADMIN_EMAIL=admin@votre-clinique.com
DEFAULT_ADMIN_PASSWORD=un-mot-de-passe-fort

# Email
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# CORS (Frontend URLs)
CORS_ALLOWED_ORIGINS=https://your-frontend.vercel.app,http://localhost:3000
CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS,PATCH
CORS_ALLOWED_HEADERS=*
CORS_ALLOW_CREDENTIALS=true
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

# Administrateur initial (créé au premier démarrage)
DEFAULT_ADMIN_EMAIL=admin@votre-clinique.com
DEFAULT_ADMIN_PASSWORD=un-mot-de-passe-fort

# Email
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# CORS (Frontend URLs)
CORS_ALLOWED_ORIGINS=https://your-frontend.netlify.app,http://localhost:3000
CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS,PATCH
CORS_ALLOWED_HEADERS=*
CORS_ALLOW_CREDENTIALS=true
```

---

## 🗄️ Schéma de base de données (Flyway)

Le schéma n'est plus créé par Hibernate : il est géré par **Flyway**, et Hibernate se contente de le **valider** au démarrage (`spring.jpa.hibernate.ddl-auto=validate`). Si le schéma ne correspond pas aux entités, l'application refuse de démarrer.

### Où sont les migrations
| Dossier | Base | Utilisé par |
|---|---|---|
| `src/main/resources/db/migration/postgresql` | PostgreSQL | Render, Railway, prod |
| `src/main/resources/db/migration/sqlserver` | SQL Server | développement local (profil `dev`) |

Flyway choisit automatiquement le dossier selon la base connectée (`spring.flyway.locations=classpath:db/migration/{vendor}`).

- `V1__schema_initial.sql` : schéma complet (les deux bases).
- `V2__contrainte_chevauchement_rendez_vous.sql` : **PostgreSQL uniquement**. Contrainte d'exclusion qui interdit, en base, deux rendez-vous actifs du même médecin sur des créneaux qui se chevauchent. Nécessite l'extension `btree_gist` (créée par la migration ; disponible sur Neon, Render et Railway).

### Modifier le modèle (nouvelle entité, nouveau champ...)
1. Modifier l'entité JPA.
2. Créer une **nouvelle** migration `V<n>__description.sql` dans **les deux dossiers** (même numéro, SQL adapté à chaque base). Prochain numéro libre : **V3**.
3. Ne **jamais** modifier une migration déjà appliquée : Flyway vérifie leur somme de contrôle et refuse de démarrer.
4. Lancer les tests `FlywayMigrationPostgreSqlTest` et `FlywayMigrationSqlServerTest` : ils appliquent les migrations puis valident le schéma contre les entités.

### Première mise en place (base existante)
Les bases créées auparavant par `ddl-auto=update` ne sont pas reconnues par Flyway (« Found non-empty schema(s) without schema history table ») et leurs colonnes `role` / `sexe` sont encore numériques (point C12). Il n'y a pas encore de données de production : **supprimer et recréer la base** (locale et de test), puis démarrer l'application. L'administrateur initial et les types de rendez-vous sont recréés automatiquement (`DEFAULT_ADMIN_EMAIL`, `DEFAULT_ADMIN_PASSWORD`).

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
- `DEFAULT_ADMIN_PASSWORD`
- Toute clé API

### Bonnes pratiques
- Utiliser des secrets forts (256+ bits pour JWT)
- Activer SSL en production
- Limiter l'exposition des endpoints Actuator
- Surveiller les logs pour les tentatives d'intrusion