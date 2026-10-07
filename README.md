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
  "password": "votre-mot-de-passe"
}
```
Administrateur initial : il est créé au premier démarrage à partir des variables d'environnement
`DEFAULT_ADMIN_EMAIL` et `DEFAULT_ADMIN_PASSWORD` (8 caractères minimum).
Si elles ne sont pas définies, aucun administrateur n'est créé et un avertissement apparaît dans les logs.
**Réponse:**
```json
{
  "token": "jwt_token",
  "user": {...}
}
```

## 📄 Pagination des listes

Les endpoints de liste acceptent une pagination **optionnelle** :

- **sans paramètre `page`** : réponse inchangée, tableau JSON complet (`[ {...}, ... ]`) ;
- **avec `page`** : réponse paginée.

```
GET /api/patients?page=0&size=20&sort=nom,asc
```
```json
{
  "content": [ {...}, ... ],
  "page": 0,
  "size": 20,
  "totalElements": 135,
  "totalPages": 7
}
```

- `page` commence à 0 ; `size` vaut 20 par défaut et **100 au maximum** ; `sort=propriete,asc|desc` (par défaut `id`). Un tri sur une propriété inconnue renvoie 400.
- Endpoints concernés : `GET /api/patients`, `/api/rendezvous`, `/api/factures`, `/api/prescriptions`, `/api/utilisateurs`, `/api/medecins`, `/api/secretaires`, `/api/administrateurs/all`, les listes `/medecin/{medecinId}` (patients, rendez-vous, factures, prescriptions) et `/api/notifications/{userId}` (triées par date décroissante).
- Les règles d'accès sont les mêmes avec ou sans pagination.
- La liste complète sans `page` sera retirée à terme : migrer les écrans vers la version paginée.

## 🔐 Droits d'accès par rôle

Toutes les routes exigent un JWT (`Authorization: Bearer <token>`), sauf `/api/auth/**`, `/actuator/health`, `/` et, si activé, Swagger.

Légende : ✅ autorisé · ❌ refusé · 👤 uniquement ses propres données (`@authz.isCurrentUser` / `@authz.owns...` : ressource rattachée, via son rendez-vous, au médecin connecté)

| Ressource / action | ADMIN | SECRETAIRE | MEDECIN |
|---|:-:|:-:|:-:|
| **Administrateurs** (toutes actions) | ✅ | ❌ | ❌ |
| **Utilisateurs** : lister, voir, contacts | ✅ | ✅ | ✅ |
| **Utilisateurs** : supprimer | ✅ | ❌ | ❌ |
| **Utilisateurs** : modifier son propre profil (`PUT /api/utilisateurs/me` : nom, prénom, téléphone, adresse) | 👤 | 👤 | 👤 |
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
| **Factures** : créer | ✅ | ✅ | 👤 (sur ses rendez-vous) |
| **Factures** : lister toutes, enregistrer un **paiement** | ✅ | ✅ | ❌ |
| **Factures** : modifier (uniquement si aucun paiement, voir I19) | ✅ | ✅ | 👤 |
| **Factures** : voir, PDF | ✅ | ✅ | 👤 |
| **Factures** d'un médecin | ✅ | ✅ | 👤 |
| **Factures** : supprimer (uniquement si aucun paiement) | ✅ | ❌ | 👤 |
| **Lignes de facture** : créer | — | — | uniquement avec la facture (`POST /api/factures`) |
| **Lignes de facture** : modifier, supprimer (uniquement si aucun paiement, total recalculé, voir I19) | ✅ | ✅ | ❌ |
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
| **Types de rendez-vous** : créer, modifier, supprimer | ✅ | ✅ | ❌ |
| **Revenus** : globaux et par médecin et par service (`/api/revenus/medecins`) | ✅ | ❌ | ❌ |
| **Messages** (conversation, lu, liste) | 👤 | 👤 | 👤 |
| **Notifications** : les siennes | ✅ (toutes) | 👤 | 👤 |
| **Notifications** : marquer comme lue | 👤 | 👤 | 👤 |
| **Notifications** : envoyer un e-mail libre | ✅ | ❌ | ❌ |

Réponses : **401** sans token valide, **403** si le rôle ou la propriété ne le permet pas, **429** après 5 échecs de connexion en 15 minutes sur un même compte.

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

#### GET /api/rendezvous/medecin/{medecinId}/aujourdhui
(ancienne forme `/aujourd'hui` toujours acceptée)
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
Enregistrer un paiement (complet ou par tranche) : `{ "montantPaiement": 5000 }`.
Chaque paiement est historisé à sa date. Refusé (400) s'il dépasse le reste à payer, ou si la facture est payée ou annulée.

#### DELETE /api/factures/{id}
Supprimer une facture (refusé avec 409 si des paiements ont été enregistrés : l'historique des revenus est conservé)

#### GET /api/factures/{id}/pdf
Générer et télécharger le PDF d'une facture

## 📊 Statistiques (ADMIN)

Les revenus sont calculés à partir des **paiements encaissés, à leur date** : un paiement par tranche compte dans le mois de chaque versement.

#### GET /api/revenus
Revenus du mois en cours, du mois précédent, et évolution en %.
**Réponse:**
```json
{
  "revenuMensuel": 125000.50,
  "revenuMoisPrecedent": 115000.75,
  "pourcentageEvolution": 8.7
}
```

#### GET /api/revenus/medecins?debut=2030-01-01&fin=2030-01-31
Revenus encaissés **par médecin et par service** sur la période (dates incluses ; par défaut le mois en cours), du médecin qui a le plus encaissé au moins.
Un paiement partiel est réparti entre les services de sa facture au prorata de leur montant.
**Réponse:**
```json
[
  {
    "medecinId": 7,
    "nom": "Martin",
    "prenom": "Paul",
    "totalEncaisse": 5000.00,
    "services": [
      { "service": "Consultation générale", "montantEncaisse": 3333.33 },
      { "service": "Pansement", "montantEncaisse": 1666.67 }
    ]
  }
]
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
