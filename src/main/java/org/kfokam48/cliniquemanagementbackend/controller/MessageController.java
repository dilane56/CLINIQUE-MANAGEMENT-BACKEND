package org.kfokam48.cliniquemanagementbackend.controller;
import org.springframework.security.access.prepost.PreAuthorize;

import lombok.RequiredArgsConstructor;
import org.kfokam48.cliniquemanagementbackend.dto.PageResponse;
import org.kfokam48.cliniquemanagementbackend.dto.message.MessageResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    // Variante paginée (?page=0&size=30), les plus récents d'abord ; sans « page », l'historique complet ci-dessus
    @GetMapping(value = "/conversation/{user1Id}/{user2Id}", params = "page")
    @PreAuthorize("@authz.isCurrentUser(#user1Id) or @authz.isCurrentUser(#user2Id)")
    public ResponseEntity<PageResponse<MessageResponseDTO>> getConversationPage(
            @PathVariable Long user1Id,
            @PathVariable Long user2Id,
            @PageableDefault(size = 30, sort = "dateEnvoi", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(chatService.getConversation(user1Id, user2Id, pageable));
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