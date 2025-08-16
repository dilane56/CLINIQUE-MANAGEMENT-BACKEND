package org.kfokam48.cliniquemanagementbackend.controlleur;

import lombok.RequiredArgsConstructor;
import org.kfokam48.cliniquemanagementbackend.dto.message.MessageDTO;
import org.kfokam48.cliniquemanagementbackend.dto.message.MessageResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.Message;
import org.kfokam48.cliniquemanagementbackend.model.Utilisateur;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.kfokam48.cliniquemanagementbackend.service.chat.ChatService;
import org.kfokam48.cliniquemanagementbackend.enums.UserStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UtilisateurRepository utilisateurRepository;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload MessageDTO chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Message savedMessage = chatService.sendMessages(chatMessage);
            MessageResponseDTO messageResponse = chatService.convertToResponseDTO(savedMessage);

            // Envoi au destinataire
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getDestinataireId().toString(),
                    "/queue/messages",
                    messageResponse
            );

            // Envoi à l'expéditeur pour confirmation
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getExpediteurId().toString(),
                    "/queue/messages",
                    messageResponse
            );

        } catch (Exception e) {
            // Envoyer un message d'erreur à l'expéditeur
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getExpediteurId().toString(),
                    "/queue/errors",
                    "Erreur lors de l'envoi du message: " + e.getMessage()
            );
        }
    }

    @MessageMapping("/chat.join")
    public void addUser(@Payload String userId, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("user_id", userId);

        // Mettre à jour le statut de l'utilisateur en ligne
        try {
            Long userIdLong = Long.parseLong(userId);
            Utilisateur user = utilisateurRepository.findById(userIdLong).orElse(null);

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
            System.err.println("Erreur lors de la mise à jour du statut utilisateur: " + e.getMessage());
        }

        messagingTemplate.convertAndSend("/topic/public", userId + " a rejoint le chat");
    }

    // Endpoints REST pour la récupération des messages
    @GetMapping("/messages/{conversationId}")
    public ResponseEntity<List<MessageResponseDTO>> getMessagesByConversation(@PathVariable Long conversationId) {
        List<MessageResponseDTO> messages = chatService.getMessagesByConversation(conversationId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/conversations/{userId}")
    public ResponseEntity<?> getUserConversations(@PathVariable Long userId) {
        return ResponseEntity.ok(chatService.getUserConversations(userId));
    }

    @GetMapping("/messages/user/{userId}")
    public ResponseEntity<List<MessageResponseDTO>> getMessagesForUser(@PathVariable Long userId) {
        List<MessageResponseDTO> messages = chatService.getMessagesForUser(userId);
        return ResponseEntity.ok(messages);
    }

    // Endpoint pour mettre à jour le statut utilisateur
    @PostMapping("/status/{userId}")
    public ResponseEntity<String> updateUserStatus(@PathVariable Long userId, @RequestParam String status) {
        try {
            Utilisateur user = utilisateurRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setStatus(UserStatus.valueOf(status));
                user.setDerniereConnexion(LocalDateTime.now());
                utilisateurRepository.save(user);

                // Notifier tous les utilisateurs du changement de statut
                messagingTemplate.convertAndSend("/topic/status",
                        String.format("{\"userId\": %d, \"status\": \"%s\", \"email\": \"%s\"}",
                                user.getId(), status, user.getEmail()));

                return ResponseEntity.ok("Statut mis à jour avec succès");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la mise à jour du statut: " + e.getMessage());
        }
    }
}
