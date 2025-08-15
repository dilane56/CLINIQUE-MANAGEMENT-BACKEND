package org.kfokam48.cliniquemanagementbackend.config;
import org.kfokam48.cliniquemanagementbackend.enums.UserStatus;
import org.kfokam48.cliniquemanagementbackend.model.Utilisateur;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionConnectEvent;

import java.time.LocalDateTime;

@Component
public class WebSocketEventListener {

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    private final UtilisateurRepository utilisateurRepository;
    public WebSocketEventListener(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    // Événement de connexion
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        // Logique pour marquer l'utilisateur comme "ONLINE"
        Utilisateur user = (Utilisateur) event.getUser(); // Assurez-vous que l'utilisateur est extrait correctement
        // Mettre à jour le statut de l'utilisateur dans la base de données
        assert user != null;
        user.setStatus(UserStatus.EN_LIGNE);
        user.setDerniereConnexion(LocalDateTime.now()); // Enregistre l'horodatage de
        utilisateurRepository.save(user);
        // Ensuite, tu peux envoyer une notification à tous les contacts de l'utilisateur.
        // Exemple : messagingTemplate.convertAndSend("/topic/status", "User X is now online");
    }

    // Événement de déconnexion
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        // Logique pour marquer l'utilisateur comme "OFFLINE"
        Utilisateur user = (Utilisateur) event.getUser(); // Assurez-vous que l'utilisateur est extrait correctement
        // Mettre à jour le statut de l'utilisateur dans la base de données
        assert user != null;
        user.setStatus(UserStatus.HORS_LIGNE);
        user.setDerniereConnexion(LocalDateTime.now()); // Enregistre l'horodatage de
        utilisateurRepository.save(user);
        // Enregistre l'horodatage de la déconnexion dans le champ lastSeen.
        // Envoie une notification à tous les contacts pour mettre à jour l'UI.
        // Exemple : messagingTemplate.convertAndSend("/topic/status", "User X is now offline");
    }
}
