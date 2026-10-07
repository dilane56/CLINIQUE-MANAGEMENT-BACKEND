package org.kfokam48.cliniquemanagementbackend.controlleur;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kfokam48.cliniquemanagementbackend.config.WebSocketAuthInterceptor;
import org.kfokam48.cliniquemanagementbackend.service.notification.NotificationService;
import org.kfokam48.cliniquemanagementbackend.dto.message.MessageDTO;
import org.kfokam48.cliniquemanagementbackend.dto.message.MessageResponseDTO;
import org.kfokam48.cliniquemanagementbackend.enums.UserStatus;
import org.kfokam48.cliniquemanagementbackend.model.Message;
import org.kfokam48.cliniquemanagementbackend.model.Utilisateur;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.kfokam48.cliniquemanagementbackend.service.chat.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.time.Instant;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload MessageDTO chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // L'expéditeur est toujours l'utilisateur authentifié de la session (impossible d'usurper un autre compte)
        chatMessage.setExpediteurId(sessionUserId(headerAccessor));
        try {
            Message savedMessage = chatService.sendMessages(chatMessage);
            MessageResponseDTO messageResponse = chatService.convertToResponseDTO(savedMessage);

            // Envoi au destinataire
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getDestinataireId().toString(),
                    "/queue/messages",
                    messageResponse
            );
            notificationService.sendNotification(savedMessage.getDestinataire().getId(), "Nouveau message", "Vous avez un nouveau message de " + savedMessage.getExpediteur().getNom(),false);

            // Envoi à l'expéditeur pour confirmation
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getExpediteurId().toString(),
                    "/queue/messages",
                    messageResponse
            );

        } catch (Exception e) {
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getExpediteurId().toString(),
                    "/queue/errors",
                    "Erreur lors de l'envoi du message: " + e.getMessage()
            );
        }
    }

    @MessageMapping("/chat.join")
    public void addUser(@Payload String userId, SimpMessageHeaderAccessor headerAccessor) {
        // L'identifiant envoyé par le client est ignoré : seul l'utilisateur authentifié au CONNECT peut se déclarer en ligne
        try {
            Long userIdLong = sessionUserId(headerAccessor);
            Utilisateur user = utilisateurRepository.findById(userIdLong).orElse(null);

            if (user != null) {
                user.setStatus(UserStatus.EN_LIGNE);
                user.setDerniereConnexion(Instant.now());
                utilisateurRepository.save(user);

                // Notifier tous les utilisateurs du changement de statut
               notificationService.sendNotification(user.getId(), "Statut mis à jour", "Votre statut a été mis à jour à EN_LIGNE", false);
                // Utilisation de DTO pour un formatage plus propre et plus sûr
                messagingTemplate.convertAndSend("/topic/status",
                        String.format("{\"userId\": %d, \"status\": \"EN_LIGNE\"}", user.getId()));
            }
        } catch (Exception e) {
            log.warn("Erreur lors de la mise à jour du statut utilisateur: " + e.getMessage());
        }
    }

    // Identifiant posé dans la session par WebSocketAuthInterceptor lors du CONNECT
    private Long sessionUserId(SimpMessageHeaderAccessor headerAccessor) {
        return Long.valueOf(headerAccessor.getSessionAttributes()
                .get(WebSocketAuthInterceptor.USER_ID_ATTRIBUTE).toString());
    }
}