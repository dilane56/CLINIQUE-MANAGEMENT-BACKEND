package org.kfokam48.cliniquemanagementbackend.controlleur;
import org.springframework.security.access.prepost.PreAuthorize;

import lombok.RequiredArgsConstructor;
import org.kfokam48.cliniquemanagementbackend.dto.message.MessageResponseDTO;
import org.kfokam48.cliniquemanagementbackend.service.chat.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final ChatService chatService;

    @GetMapping("/conversation/{user1Id}/{user2Id}")
    @PreAuthorize("@authz.isCurrentUser(#user1Id) or @authz.isCurrentUser(#user2Id)")
    public ResponseEntity<List<MessageResponseDTO>> getConversation(
            @PathVariable Long user1Id,
            @PathVariable Long user2Id) {
        try {
            List<MessageResponseDTO> messages = chatService.getConversation(user1Id, user2Id);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/mark-as-read/{senderId}/{recipientId}")
    @PreAuthorize("@authz.isCurrentUser(#senderId) or @authz.isCurrentUser(#recipientId)")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable Long senderId,
            @PathVariable Long recipientId) {
        try {
            chatService.markMessagesAsRead(senderId, recipientId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Le reste des méthodes REST, comme getUserConversations
    @GetMapping("/conversations/{userId}")
    @PreAuthorize("@authz.isCurrentUser(#userId)")
    public ResponseEntity<?> getUserConversations(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(chatService.getUserConversations(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}