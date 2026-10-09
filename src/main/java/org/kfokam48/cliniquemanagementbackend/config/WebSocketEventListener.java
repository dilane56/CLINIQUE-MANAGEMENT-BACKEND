package org.kfokam48.cliniquemanagementbackend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kfokam48.cliniquemanagementbackend.enums.UserStatus;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.kfokam48.cliniquemanagementbackend.model.Utilisateur;

import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final UtilisateurRepository utilisateurRepository;
    private final SimpUserRegistry simpUserRegistry;

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
                    log.warn("ID utilisateur non valide lors de la connexion WebSocket.");
                }
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String userIdStr = (String) headerAccessor.getSessionAttributes().get("user_id");

        if (userIdStr != null) {
            if (autreSessionOuverte(event)) {
                // Un autre onglet reste connecté : l'utilisateur est toujours en ligne
                return;
            }
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
                log.warn("ID utilisateur non valide lors de la déconnexion WebSocket.");
            }
        }
    }

    // Le frontend ouvre une connexion par onglet : seule la fermeture de la dernière met hors ligne
    private boolean autreSessionOuverte(SessionDisconnectEvent event) {
        Principal principal = event.getUser();
        if (principal == null) {
            return false;
        }
        SimpUser simpUser = simpUserRegistry.getUser(principal.getName());
        return simpUser != null && simpUser.getSessions().stream()
                .anyMatch(session -> !session.getId().equals(event.getSessionId()));
    }
}