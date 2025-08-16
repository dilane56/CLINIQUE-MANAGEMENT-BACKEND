# Guide d'utilisation du système de messagerie WebSocket

## Vue d'ensemble

Ce système de messagerie en temps réel utilise WebSocket avec STOMP pour permettre l'échange de messages entre utilisateurs de manière instantanée.

## Configuration

### 1. Endpoints WebSocket

- **Endpoint principal** : `/ws-chat`
- **Avec SockJS** : `/ws-chat` (pour les navigateurs qui ne supportent pas WebSocket natif)
- **Sans SockJS** : `/ws-chat` (pour les clients natifs)

### 2. Destinations de messages

- **Messages privés** : `/user/{userId}/queue/messages`
- **Erreurs** : `/user/{userId}/queue/errors`
- **Statuts** : `/topic/status`
- **Messages publics** : `/topic/public`

### 3. Endpoints d'application

- **Envoi de message** : `/app/chat.send`
- **Rejoindre le chat** : `/app/chat.join`

## Utilisation côté client

### Connexion WebSocket

```javascript
// Connexion avec SockJS
const socket = new SockJS('http://localhost:8080/ws-chat');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame) {
    console.log('Connecté: ' + frame);
    
    // S'abonner aux messages privés
    stompClient.subscribe('/user/' + userId + '/queue/messages', function (message) {
        console.log('Message reçu:', JSON.parse(message.body));
    });
    
    // S'abonner aux erreurs
    stompClient.subscribe('/user/' + userId + '/queue/errors', function (error) {
        console.error('Erreur:', error.body);
    });
    
    // Rejoindre le chat
    stompClient.send("/app/chat.join", {}, userId);
});
```

### Envoi de message

```javascript
const chatMessage = {
    expediteurId: 1,
    destinataireId: 2,
    contenu: "Bonjour !"
};

stompClient.send("/app/chat.send", {}, JSON.stringify(chatMessage));
```

### Déconnexion

```javascript
stompClient.disconnect();
```

## API REST

### Récupération des messages

- **Messages par conversation** : `GET /api/chat/messages/{conversationId}`
- **Conversations d'un utilisateur** : `GET /api/chat/conversations/{userId}`
- **Messages d'un utilisateur** : `GET /api/chat/messages/user/{userId}`

## Structure des messages

### MessageDTO (envoi)

```json
{
    "expediteurId": 1,
    "destinataireId": 2,
    "contenu": "Contenu du message"
}
```

### MessageResponseDTO (réception)

```json
{
    "id": 1,
    "contenu": "Contenu du message",
    "dateEnvoi": "2024-01-01T12:00:00",
    "lu": false,
    "expediteur": {
        "id": 1,
        "nom": "John Doe",
        "email": "john@example.com"
    },
    "destinataire": {
        "id": 2,
        "nom": "Jane Smith",
        "email": "jane@example.com"
    }
}
```

## Test du système

1. **Démarrer l'application Spring Boot**
2. **Ouvrir le fichier `websocket-client-example.html`** dans un navigateur
3. **Entrer les IDs utilisateur** (assurez-vous que ces utilisateurs existent en base)
4. **Cliquer sur "Se connecter"**
5. **Envoyer des messages**

## Gestion des erreurs

Le système gère automatiquement :
- Les erreurs de connexion
- Les utilisateurs non trouvés
- Les erreurs de base de données
- Les messages d'erreur sont envoyés via `/user/{userId}/queue/errors`

## Statuts utilisateur

- **EN_LIGNE** : Utilisateur connecté au WebSocket
- **HORS_LIGNE** : Utilisateur déconnecté
- Les changements de statut sont diffusés via `/topic/status`

## Sécurité

- Les WebSockets sont autorisés pour tous les utilisateurs (`/ws-chat/**` dans SecurityConfig)
- L'authentification JWT peut être ajoutée dans `WebSocketAuthInterceptor`
- Les messages sont validés côté serveur

## Dépannage

### Problèmes courants

1. **Connexion échoue** : Vérifiez que l'application Spring Boot est démarrée
2. **Messages non reçus** : Vérifiez que les IDs utilisateur existent en base
3. **Erreurs CORS** : Vérifiez la configuration des origines autorisées

### Logs utiles

- Les connexions/déconnexions sont loggées dans la console
- Les erreurs sont affichées dans la console du navigateur
- Les erreurs serveur sont loggées dans les logs Spring Boot
