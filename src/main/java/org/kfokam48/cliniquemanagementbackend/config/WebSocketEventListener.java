package org.kfokam48.cliniquemanagementbackend.config;

import lombok.RequiredArgsConstructor;
import org.kfokam48.cliniquemanagementbackend.enums.UserStatus;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.kfokam48.cliniquemanagementbackend.model.Utilisateur;

import java.time.Instant;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final UtilisateurRepository utilisateurRepository;

    // Dans votre classe WebSocketEventListener.java
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // Vérifier si les attributs de session ne sont pas null
        if (headerAccessor.getSessionAttributes() != null) {
            String userIdStr = (String) headerAccessor.getSessionAttributes().get("user_id");

            if (userIdStr != null) {
                try {
                    Long userId = Long.parseLong(userIdStr);
                    Utilisateur user = utilisateurRepository.findById(userId).orElse(null);
                    if (user != null && user.getStatus() != UserStatus.EN_LIGNE) {
                        user.setStatus(UserStatus.EN_LIGNE);
                        utilisateurRepository.save(user);

                        messagingTemplate.convertAndSend("/topic/status",
                                String.format("{\"userId\": %d, \"status\": \"EN_LIGNE\"}", user.getId()));
                    }
                } catch (NumberFormatException e) {
                    System.err.println("ID utilisateur non valide lors de la connexion WebSocket.");
                }
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String userIdStr = (String) headerAccessor.getSessionAttributes().get("user_id");

        if (userIdStr != null) {
            try {
                Long userId = Long.parseLong(userIdStr);
                Utilisateur user = utilisateurRepository.findById(userId).orElse(null);
                if (user != null) {
                    user.setStatus(UserStatus.HORS_LIGNE);
                    user.setDerniereConnexion(Instant.now());
                    utilisateurRepository.save(user);

                    messagingTemplate.convertAndSend("/topic/status",
                            String.format("{\"userId\": %d, \"status\": \"HORS_LIGNE\", \"derniereConnexion\": \"%s\"}",
                                    user.getId(), Instant.now().toString()));
                }
            } catch (NumberFormatException e) {
                System.err.println("ID utilisateur non valide lors de la déconnexion WebSocket.");
            }
        }
    }
}