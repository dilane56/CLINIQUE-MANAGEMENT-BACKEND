# Audit du backend — Clinique Management

> Date de l'audit : 2026-10-06
> Base analysée : branche `main`, commit `9dd22b2` (+ modification locale de `AuthService.java`)
>
> Ce fichier sert de **liste de contrôle**. Cochez chaque case (`[x]`) une fois le point corrigé **et vérifié**,
> et notez le commit correspondant dans la colonne « Commit / Notes » si utile.

---

## Légende

- 🔴 **Critique** : bug bloquant ou faille de sécurité, à corriger en priorité
- 🟠 **Important** : risque réel en production ou dette technique significative
- 🟡 **Amélioration** : qualité, maintenabilité, propreté

---

## ✅ Points forts constatés (à préserver)

- Stack moderne : Spring Boot 3.4.5, Java 17, JJWT 0.12, Spring Security stateless, Validation, Actuator, Springdoc.
- Architecture en couches claire : `controlleur` → `service` (interface + `impl`) → `repository`, DTO requête/réponse séparés, mappers dédiés.
- Logique métier réelle : détection des chevauchements de rendez-vous (patient **et** médecin, y compris en mise à jour via `...AndIdNot`), scheduler de statuts, factures + PDF, prescriptions, revenus, chat WebSocket.
- Gestion d'erreurs centralisée (`GlobalExceptionHandler`) avec codes HTTP cohérents.
- Aucun secret en dur dans les fichiers `.properties` : tout passe par des variables d'environnement.
- Clé JWT décodée une seule fois au démarrage (`@PostConstruct`).
- Optimisations : `@Transactional(readOnly = true)` sur les lectures, envoi d'e-mails asynchrone (`@Async`).
- Déploiement outillé : Dockerfile, profils `render` / `railway` / `prod`, healthchecks Actuator, CI GitHub Actions (build + tests + smoke test Docker).
- Documentation : `README.md`, `DEPLOYMENT_GUIDE.md`, `WEBSOCKET_README.md`, diagrammes UML.

---

## 🔴 1. Critiques — bugs et sécurité

### 1.1 Compilation

- [x] **C1** — `service/auth/AuthService.java:60` : faute de frappe `loag.info(...)` au lieu de `log.info(...)` (modification locale non commitée) → **le projet ne compile pas**.
  - Vérification : `./mvnw clean compile` passe.

### 1.2 Fuite des mots de passe (hash BCrypt) dans les réponses API

- [x] **C2** — Des entités JPA sont renvoyées directement par les contrôleurs, et aucun `@JsonIgnore` n'existe dans le projet → le champ `password` est sérialisé en JSON.
  - `controlleur/UtilisateurController.java` : `GET /api/utilisateurs/{id}` renvoie `Utilisateur`
  - `controlleur/AdministrateurController.java` : `create`, `all`, `{id}`, `update/{id}`, `email/{email}` renvoient `Administrateur`
  - `controlleur/MedecinController.java` : `POST /api/medecins` renvoie `Medecin`
  - Correction attendue : renvoyer uniquement des DTO de réponse (et, par sécurité, `@JsonProperty(access = WRITE_ONLY)` ou `@JsonIgnore` sur `Utilisateur.password`).
  - Vérification : aucune réponse JSON ne contient la clé `password`.

### 1.3 Endpoints exposés publiquement ou sans contrôle de rôle

- [x] **C3** — `config/SecurityConfig.java` : liste `permitAll` trop large : `/api/utilisateurs`, `/api/medecins`, `/api/secretaires`, `/api/patient`, `/api/administrateurs/create`.
  - Conséquence : `GET /api/utilisateurs` (liste de **tous** les utilisateurs) est accessible **sans authentification**.
  - Correction attendue : ne laisser publics que `/api/auth/**`, la doc (si souhaitée hors prod), `/actuator/health`, et le endpoint WebSocket (authentifié au niveau STOMP).
- [x] **C4** — `controlleur/UtilisateurController.java` : aucun `@PreAuthorize` → n'importe quel utilisateur connecté peut **supprimer n'importe quel compte** (`DELETE /api/utilisateurs/{id}`) ou lire n'importe quel profil.
- [x] **C5** — `controlleur/AdministrateurController.java` : `PUT /update/{id}`, `DELETE /delete/{id}`, `GET /email/{email}` sans `@PreAuthorize`.
- [x] **C6** — Contrôleurs entièrement sans `@PreAuthorize` (tout utilisateur authentifié a tous les droits) :
  - [x] `FactureController`
  - [x] `LigneFactureController`
  - [x] `PrescriptionController`
  - [x] `LignePrescriptionController`
  - [x] `RendezVousController`
  - [x] `RevenuController`
  - [x] `TypeRendezVousController`
  - [x] `MessageController`
  - [x] `notification/NotificationRestController`
- [x] **C7** — Absence de contrôle de propriété : un médecin peut consulter les données d'un autre médecin via les routes `/medecin/{medecinId}` (rendez-vous, factures, patients). Vérifier que l'ID demandé correspond à l'utilisateur connecté (sauf ADMIN / SECRETAIRE rattachée).
- [x] **C8** — `controlleur/MedecinController.java:40` : faute de frappe `hasRole('SECRETARE')` → les secrétaires n'ont jamais accès à `GET /api/medecins/{id}`.

### 1.4 Compte administrateur par défaut

- [x] **C9** — `config/DefaultUserInitializer.java` : création systématique (y compris en production) d'un admin `admin@gmail.com` / `password`.
  - Correction attendue : identifiants lus depuis des variables d'environnement (`ADMIN_EMAIL`, `ADMIN_PASSWORD`), ou création limitée au profil `dev` ; jamais de mot de passe par défaut en prod.

### 1.5 WebSocket non sécurisé

- [x] **C10** — `config/WebSocketAuthInterceptor.java` : aucune vérification du JWT à la trame `CONNECT` (toutes les connexions acceptées) ; `System.out.println` utilisé.
- [x] **C11** — `config/WebSocketConfig.java` : `setAllowedOriginPatterns("*")` → réutiliser la liste `cors.allowed.origins`.
  - Vérification : une connexion STOMP sans token valide est refusée.
- [x] **C16** (découvert pendant C10) — L'intercepteur WebSocket n'était **pas enregistré** (`configureClientInboundChannel` absent). Comme `/user` est aussi un préfixe du broker simple, **n'importe qui pouvait s'abonner à `/user/{idAutreUtilisateur}/queue/messages`** (lecture des messages privés) ou **envoyer directement** vers cette destination (usurpation). L'`expediteurId` et l'ID de `/app/chat.join` venaient du client.
  - Corrigé : abonnements limités à `/topic/**` et à ses propres files ; envois limités à `/app/**` ; expéditeur et utilisateur « en ligne » pris dans la session authentifiée.

### 1.6 Stockage du rôle

- [x] **C12** — `model/Utilisateur.java` : le champ `role` n'a pas `@Enumerated(EnumType.STRING)` → stocké en **ordinal**. Réordonner l'enum `Roles` changerait silencieusement les droits des utilisateurs.
  - Même problème trouvé et corrigé sur `model/Patient.java` : `sexe` (enum `Sexe`).
  - Pas de production en cours (seulement un déploiement de test) → aucune migration de données de prod nécessaire.
  - ⚠️ **Base locale / base de test existante** : `ddl-auto=update` ne change **pas** le type d'une colonne existante (elle reste numérique). Deux options :
    1. **Recommandé** : supprimer et recréer la base (l'admin et les types de rendez-vous sont recréés au démarrage).
    2. Conserver les données (PostgreSQL) :
       ```sql
       ALTER TABLE utilisateur DROP CONSTRAINT IF EXISTS utilisateur_role_check;
       ALTER TABLE utilisateur ALTER COLUMN role TYPE varchar(255)
         USING CASE role WHEN 0 THEN 'MEDECIN' WHEN 1 THEN 'ADMIN' WHEN 2 THEN 'SECRETAIRE' END;
       ALTER TABLE patient DROP CONSTRAINT IF EXISTS patient_sexe_check;
       ALTER TABLE patient ALTER COLUMN sexe TYPE varchar(255)
         USING CASE sexe WHEN 0 THEN 'HOMME' WHEN 1 THEN 'FEMME' END;
       ```
  - Vérification : `SELECT DISTINCT role FROM utilisateur;` renvoie `ADMIN`, `MEDECIN`, `SECRETAIRE` (texte).

---

## 🟠 2. Importants

### 2.1 Sécurité / configuration

- [ ] **I1** — CORS configuré deux fois (`config/SecurityConfig.java` et `config/WebConfig.java`) → n'en garder qu'une (un bean `CorsConfigurationSource`).
- [ ] **I2** — Swagger (`springdoc.*.enabled=true`) actif dans tous les profils, y compris prod/render/railway → désactiver ou protéger en production.
- [ ] **I3** — `management.endpoint.health.show-details=always` en production → passer à `when-authorized` ou `never`.
- [ ] **I4** — JWT valide 24 h, sans refresh token ni mécanisme de révocation.
- [ ] **I5** — `config/JwtRequestFillter.java` : le statut de l'utilisateur (`UserStatus`, ex. SUSPENDED/INACTIVE) n'est pas vérifié → un compte suspendu garde son accès tant que le token est valide (vérifier aussi `CustomUserDetails.isEnabled()` / `isAccountNonLocked()`).
- [ ] **I6** — Pas de limitation de tentatives (rate limiting / anti brute-force) sur `/api/auth/login`.
- [ ] **I7** — `JwtRequestFillter.sendUnauthorizedResponse` construit le JSON par concaténation de chaînes → utiliser `ObjectMapper`.
- [ ] **I8** — `application.properties` (profil par défaut) contient une configuration **SQL Server** alors que les autres profils utilisent PostgreSQL → harmoniser.

### 2.2 Données / persistance

- [ ] **I9** — `spring.jpa.hibernate.ddl-auto=update` dans **tous** les profils → introduire **Flyway** (ou Liquibase) et passer à `validate` en production.
- [ ] **I10** — Aucun verrou optimiste (`@Version`) ni contrainte en base → deux réservations simultanées peuvent passer le contrôle de chevauchement dans `RendezVousServiceImpl` (race condition). Ajouter `@Version` et/ou un verrou / contrainte d'exclusion.
- [ ] **I11** — Aucune pagination : les `findAll()` renvoient des tables entières (rendez-vous, patients, factures, utilisateurs…) → utiliser `Pageable` / `Page<T>`.

### 2.3 Architecture

- [ ] **I12** — `service/impl/RendezVousServiceImpl.java` injecte `NotificationController` (inversion de couches) → injecter `NotificationService`.
- [ ] **I13** — Les services renvoient des `ResponseEntity<String>` (`deleteById` dans plusieurs services) → les services doivent renvoyer du métier (`void` / exception), la réponse HTTP est construite dans le contrôleur.
- [ ] **I14** — Les contrôleurs injectent les implémentations (`*ServiceImpl`) au lieu des interfaces (`*Service`).

### 2.4 Dépendances (`pom.xml`)

- [ ] **I15** — `itextpdf 5.5.13.4` est sous licence **AGPL** → risque juridique en usage commercial ; envisager OpenPDF ou Apache PDFBox.
- [ ] **I16** — `mssql-jdbc 13.5.1.jre11-preview` (version *preview*) alors que la cible est PostgreSQL → supprimer si inutile.
- [ ] **I17** — ModelMapper coexiste avec des mappers manuels → choisir une approche unique (idéalement MapStruct, vérifié à la compilation).

### 2.5 Tests

- [ ] **I19** (découvert pendant A11) — `PUT` / `DELETE /api/lignes-facture/{id}` modifient ou suppriment une ligne **sans recalculer `montantTotal` ni `montantRestant` de la facture**, y compris sur une facture déjà payée ou partiellement payée. Les montants de la facture deviennent faux (et donc les revenus). À décider : recalculer la facture à chaque modification de ligne, ou interdire la modification des lignes d'une facture non `NON_PAYEE` (et passer par `PUT /api/factures/{id}`).
- [ ] **I18** — Un seul test (`CliniqueManagementBackendApplicationTests.contextLoads`). À ajouter au minimum :
  - [ ] Tests unitaires `RendezVousServiceImpl` (chevauchements patient/médecin, mise à jour, changement de statut)
  - [ ] Tests unitaires facturation / revenus
  - [x] Tests de sécurité `@WebMvcTest` + `@WithMockUser` pour **chaque règle d'accès** (points C3 à C8)
  - [x] Test vérifiant l'absence du champ `password` dans les réponses (point C2)
  - [ ] Tests d'intégration avec Testcontainers PostgreSQL

---

## 🟡 3. Améliorations / propreté

- [ ] **A1** — Doublons WebSocket : `WebSocketEventListner.java` (racine) et `config/WebSocketEventListener.java` → n'en garder qu'un.
- [ ] **A2** — `ChatHandler.java` à la racine du package → déplacer dans un package dédié (`websocket` / `chat`).
- [ ] **A3** — Fautes de frappe dans les noms :
  - [ ] `JwtRequestFillter` → `JwtRequestFilter`
  - [ ] `WebSocketEventListner` → `WebSocketEventListener`
  - [ ] `TypeRencezVousMapper` → `TypeRendezVousMapper`
  - [ ] `NotifcationDTO` → `NotificationDTO`
  - [ ] package `controlleur` → `controller` (optionnel, impact large)
- [ ] **A4** — Route `GET /api/rendezvous/medecin/{medecinId}/aujourd'hui` contient une apostrophe → renommer (ex. `/aujourdhui` ou `/today`) en coordination avec le frontend.
- [ ] **A5** — Remplacer les `System.out.println` par le logger SLF4J.
- [ ] **A6** — Fichiers parasites versionnés : `pom-fixed.xml`, `prescription.pdf` → supprimer du dépôt si inutiles.
- [ ] **A7** — `@EnableGlobalMethodSecurity` (déprécié) → `@EnableMethodSecurity`.
- [ ] **A8** — Commentaires `@PreAuthorize` incohérents avec la règle réelle (ex. « MEDECIN, ADMIN et SECRETAIRE » alors que seul ADMIN/SECRETAIRE est autorisé) → aligner après la revue des droits.
- [ ] **A9** (partiel) — `DefaultUserInitializer` : `AdministrateurRepository.findByEmail` renvoie `null` au lieu d'`Optional` → l'initialiseur utilise désormais `UtilisateurRepository.existsByEmail` (couvre aussi un email déjà pris par un médecin ou une secrétaire). `AdministrateurRepository.findByEmail` reste à harmoniser ailleurs.
- [x] **A11** (découvert pendant C7b) — Création isolée de lignes **sans les rattacher à leur parent** (le DTO n'a pas d'identifiant de parent) : lignes orphelines.
  - [x] `POST /api/lignes-prescription` **supprimé** (les lignes sont toujours créées avec la prescription, confirmé par le propriétaire du projet), ainsi que `LignePrescriptionService.ajouterLigne`.
  - [x] `POST /api/lignes-facture` **supprimé** (les lignes sont toujours créées avec la facture, confirmé par le propriétaire du projet), ainsi que `LigneFactureService.ajouterLigne`.
- [ ] **A10** — Matrice des droits (qui peut faire quoi par rôle) à documenter dans le README une fois les points C3–C8 corrigés (voir section 4 ci-dessous).

---

## 🔐 4. Matrice des droits appliquée (C3 → C8)

Légende : ✅ autorisé · ❌ refusé · 👤 uniquement ses propres données (`@authz.isCurrentUser` / `@authz.owns...` : ressource rattachée, via son rendez-vous, au médecin connecté)
Règles fixées par le propriétaire du projet : **la secrétaire met à jour les factures (paiement par tranche)** ; **un médecin ne peut pas supprimer de patient** ; **la secrétaire consulte et imprime les prescriptions sans les modifier** ; **les revenus (revenus générés par les médecins à travers les services) sont réservés à l'admin, le médecin ne voit pas ses revenus** ; **seul le destinataire marque sa notification comme lue**.
Les autres règles sont des choix par défaut validés implicitement ; à revoir si un écran du frontend reçoit un 403 inattendu.

Routes publiques (sans JWT) : `/`, `/health`, `/actuator/health/**`, `/api/auth/**`, Swagger, `/ws-chat/**` (le WebSocket sera sécurisé avec C10).

| Ressource / action | ADMIN | SECRETAIRE | MEDECIN |
|---|:-:|:-:|:-:|
| **Administrateurs** (toutes actions) | ✅ | ❌ | ❌ |
| **Utilisateurs** : lister, voir, contacts | ✅ | ✅ | ✅ |
| **Utilisateurs** : supprimer | ✅ | ❌ | ❌ |
| **Médecins** : créer, supprimer | ✅ | ❌ | ❌ |
| **Médecins** : lister | ✅ | ✅ | ❌ |
| **Médecins** : voir un médecin | ✅ | ✅ | ✅ |
| **Médecins** : modifier | ✅ | ❌ | 👤 |
| **Secrétaires** : créer, lister, supprimer | ✅ | ❌ | ✅ |
| **Secrétaires** : voir | ✅ | 👤 | ✅ |
| **Secrétaires** : modifier | ✅ | 👤 | ❌ |
| **Patients** : créer | ✅ | ✅ | ❌ |
| **Patients** : lister, voir, modifier | ✅ | ✅ | ✅ |
| **Patients** : supprimer | ✅ | ✅ | ❌ |
| **Patients** d'un médecin | ✅ | ✅ | 👤 |
| **Rendez-vous** : créer | ✅ | ✅ | 👤 (pour lui-même) |
| **Rendez-vous** : voir, modifier (sans le réattribuer), changer le statut | ✅ | ✅ | 👤 |
| **Rendez-vous** : lister tout, supprimer | ✅ | ✅ | ❌ |
| **Rendez-vous** d'un médecin / du jour | ✅ | ✅ | 👤 |
| **Factures** : créer, lister, modifier, **paiement** | ✅ | ✅ | ❌ |
| **Factures** : voir, PDF | ✅ | ✅ | 👤 |
| **Factures** d'un médecin | ✅ | ✅ | 👤 |
| **Factures** : supprimer | ✅ | ❌ | ❌ |
| **Lignes de facture** : créer | — | — | uniquement avec la facture (`POST /api/factures`) |
| **Lignes de facture** : modifier, supprimer (voir I19) | ✅ | ✅ | ❌ |
| **Lignes de facture** : voir une ligne | ✅ | ✅ | 👤 |
| **Lignes de facture** : lister toutes | ✅ | ✅ | ❌ |
| **Prescriptions** : créer (sur son rendez-vous), modifier | ❌ | ❌ | 👤 |
| **Prescriptions** : voir, imprimer (PDF) | ✅ | ✅ | 👤 |
| **Prescriptions** : supprimer | ✅ | ❌ | 👤 |
| **Prescriptions** : lister tout | ✅ | ✅ | ❌ |
| **Prescriptions** d'un médecin | ✅ | ✅ | 👤 |
| **Lignes de prescription** : créer | — | — | uniquement avec la prescription (`POST /api/prescriptions`) |
| **Lignes de prescription** : modifier, supprimer | ❌ | ❌ | 👤 |
| **Lignes de prescription** : voir une ligne | ✅ | ✅ | 👤 |
| **Lignes de prescription** : lister toutes | ✅ | ✅ | ❌ |
| **Types de rendez-vous** : lister | ✅ | ✅ | ✅ |
| **Types de rendez-vous** : créer, modifier, supprimer | ✅ | ❌ | ❌ |
| **Revenus** (revenus des médecins par service) | ✅ | ❌ | ❌ |
| **Messages** (conversation, lu, liste) | 👤 | 👤 | 👤 |
| **Notifications** : les siennes | ✅ (toutes) | 👤 | 👤 |
| **Notifications** : marquer comme lue | 👤 | 👤 | 👤 |
| **Notifications** : envoyer un e-mail libre | ✅ | ❌ | ❌ |

Vérification : `src/test/java/.../security/AccessControlTest.java` (31 tests) et `AuthorizationServiceTest` (4 tests).

### Points résiduels identifiés pendant C3 → C8

- [x] **C7b** — Le contrôle de propriété ne couvre que les routes `/medecin/{medecinId}`. Les routes par identifiant de ressource (`GET /api/factures/{id}`, `/api/rendezvous/{id}`, `/api/prescriptions/{id}`, PDF…) ne vérifient pas que la ressource appartient au médecin connecté. Il faut une vérification dans le service (ex. `@authz.ownsRendezVous(#id)`).
- [x] **C13** — `PUT /api/notifications/{id}/read` : n'importe quel membre du personnel peut marquer comme lue la notification d'un autre utilisateur.
- [x] **C14** — `POST /api/notifications/send` : tout utilisateur connecté peut envoyer un e-mail arbitraire depuis l'adresse de la clinique (relais de mail) ; de plus `MailDTO` n'a pas `@RequestBody`. Restreindre à ADMIN ou supprimer si inutilisé.
- [ ] **C15** — `GET /api/utilisateurs/{id}` et la liste exposent l'e-mail et le téléphone de tout le personnel à tout le personnel : acceptable pour l'annuaire interne / le chat, à confirmer.

---

## 📋 Ordre de correction recommandé

1. **C1** — rétablir la compilation
2. **C2** — supprimer la fuite des mots de passe
3. **C3 → C8** — verrouiller les accès (+ tests de sécurité I18)
4. **C12** — rôle en `STRING` + migration des données
5. **C9 → C11** — admin par défaut, WebSocket
6. **I9 → I11** — Flyway, concurrence, pagination
7. Reste des points 🟠 puis 🟡

---

## Suivi des corrections

| Point | Date | Commit | Notes |
|-------|------|--------|-------|
| C1 | 2026-10-06 | (aucun) | Faute jamais commitée : le fichier redevient identique à `main`. `log.info` rétabli dans `AuthService` ; `mvnw compile` OK |
| C2 | 2026-10-06 | `0576be2` | `@JsonProperty(WRITE_ONLY)` sur `Utilisateur.password` ; `AdministrateurController`, `MedecinController` (POST) et `UtilisateurController` (GET /{id}) renvoient des DTO. ⚠️ Les réponses admin ne contiennent plus `adresse`, `status`, `dateCreation`, `derniereConnexion` (vérifier le frontend). Test : `UtilisateurSerializationTest` |
| C3 | 2026-10-06 | `88bf24b` | `SecurityConfig` : `permitAll` limité à auth, santé, Swagger et `/ws-chat/**` |
| C4–C6 | 2026-10-06 | `88bf24b` | `@PreAuthorize` sur **toutes** les routes REST selon la matrice (section 4) |
| C7 | 2026-10-06 | `88bf24b` | Nouveau bean `service/auth/AuthorizationService` (`@authz.isCurrentUser`) appliqué aux routes `/medecin/{medecinId}`, profils médecin/secrétaire, messages et notifications. Reste C7b |
| C8 | 2026-10-06 | `88bf24b` | `SECRETARE` → `hasAnyRole('ADMIN','SECRETAIRE','MEDECIN')` |
| Droits v2 | 2026-10-06 | `88bf24b` | Secrétaire : lecture + PDF des prescriptions et lignes (pas de modification). Revenus : ADMIN uniquement (confirmé). |
| C13 | 2026-10-06 | `88bf24b` | `@authz.isNotificationRecipient(#id)` sur `PUT /api/notifications/{id}/read` |
| Tests | 2026-10-06 | `88bf24b` | `AccessControlTest` (19) + `UtilisateurSerializationTest` (2) : 21/21 OK |
| C12 | 2026-10-06 | `913a1ed` | `@Enumerated(EnumType.STRING)` sur `Utilisateur.role` et `Patient.sexe`. Base locale à recréer (ou script SQL du point C12). Tests 21/21 OK |
| C9 | 2026-10-06 | `ce81b9e` | Admin initial lu depuis `DEFAULT_ADMIN_EMAIL` / `DEFAULT_ADMIN_PASSWORD` (8 car. min.) ; sinon aucun admin créé + avertissement. README, `DEPLOYMENT_GUIDE.md`, `render.yaml` mis à jour. Test : `DefaultUserInitializerTest` (4). Total 25/25 OK |
| C10, C11, C16 | 2026-10-06 | `0f85b91` | Nouveau `service/auth/JwtService` (validation JWT partagée, utilisée par `JwtRequestFillter`). `WebSocketAuthInterceptor` enregistré : JWT obligatoire au CONNECT, contrôle SUBSCRIBE/SEND. Origines WebSocket = `cors.allowed.origins`. `WebSocketController` : expéditeur = utilisateur de la session. ⚠️ Frontend : envoyer `{ Authorization: 'Bearer ' + token }` au `connect`. Doc : `WEBSOCKET_README.md`, `websocket-client-example.html`. Test : `WebSocketAuthInterceptorTest` (8). Total 33/33 OK |
| C14 | 2026-10-06 | `71b38a6` | `POST /api/notifications/send` réservé à l'ADMIN ; `@Valid @RequestBody` + validation de `MailDTO` (email valide, sujet ≤ 200, message ≤ 5000). ⚠️ Le corps doit maintenant être du JSON |
| C7b | 2026-10-06 | `dd7e4fe` | `@authz.ownsRendezVous/ownsFacture/ownsPrescription/ownsLignePrescription/ownsLigneFacture` (requêtes `existsById...Medecin_Id`). Un médecin ne crée un rendez-vous que pour lui-même, ne le réattribue pas, ne crée une prescription que sur son rendez-vous. Listes complètes des lignes : ADMIN/SECRETAIRE. ⚠️ Requêtes dérivées validées au démarrage de Spring Data uniquement (tests unitaires avec mocks) |
| Tests | 2026-10-06 | `dd7e4fe` | `AccessControlTest` (31) + `AuthorizationServiceTest` (4) + autres : 49/49 OK |
| A11 (prescriptions) | 2026-10-06 | `2da3f00` | `POST /api/lignes-prescription` et `ajouterLigne` supprimés |
| A11 (factures) | 2026-10-06 | `662ca7f` | `POST /api/lignes-facture` et `ajouterLigne` supprimés. Nouveau point I19 (totaux de facture non recalculés) |
