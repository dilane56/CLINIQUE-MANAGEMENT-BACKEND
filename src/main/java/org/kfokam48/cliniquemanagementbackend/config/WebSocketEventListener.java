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
import org.springframework.web.socket.messaging.SessionConnectedEvent;

import java.security.Principal;
import java.time.LocalDateTime;

@Component
public class WebSocketEventListener {

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    private final UtilisateurRepository utilisateurRepository;
    
    public WebSocketEventListener(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    // Événement de connexion WebSocket
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        try {
            Principal principal = event.getUser();
            if (principal == null) {
                return;
            }

            String email = principal.getName();
            Utilisateur user = utilisateurRepository.findByEmail(email).orElse(null);

            if (user != null) {
                user.setStatus(UserStatus.EN_LIGNE);
                user.setDerniereConnexion(LocalDateTime.now());
                utilisateurRepository.save(user);

                // Notifier tous les utilisateurs du changement de statut
                messagingTemplate.convertAndSend("/topic/status", 
                    String.format("{\"userId\": %d, \"status\": \"EN_LIGNE\", \"email\": \"%s\"}", 
                    user.getId(), user.getEmail()));
            }
        } catch (Exception e) {
            // Log l'erreur mais ne pas faire échouer la connexion
            System.err.println("Erreur lors de la gestion de la connexion WebSocket: " + e.getMessage());
        }
    }

    // Événement de connexion établie
    @EventListener
    public void handleWebSocketConnectedListener(SessionConnectedEvent event) {
        // La connexion est maintenant établie
        System.out.println("Nouvelle connexion WebSocket établie");
    }

    // Événement de déconnexion
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        try {
            Principal principal = event.getUser();
            if (principal == null) {
                return;
            }

            String email = principal.getName();
            Utilisateur user = utilisateurRepository.findByEmail(email).orElse(null);

            if (user != null) {
                user.setStatus(UserStatus.HORS_LIGNE);
                user.setDerniereConnexion(LocalDateTime.now());
                utilisateurRepository.save(user);

                // Notifier tous les utilisateurs du changement de statut
                messagingTemplate.convertAndSend("/topic/status", 
                    String.format("{\"userId\": %d, \"status\": \"HORS_LIGNE\", \"email\": \"%s\"}", 
                    user.getId(), user.getEmail()));
            }
        } catch (Exception e) {
            // Log l'erreur mais ne pas faire échouer la déconnexion
            System.err.println("Erreur lors de la gestion de la déconnexion WebSocket: " + e.getMessage());
        }
    }
}
