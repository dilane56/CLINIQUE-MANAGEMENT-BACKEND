package org.kfokam48.cliniquemanagementbackend.controller;

import lombok.RequiredArgsConstructor;
import org.kfokam48.cliniquemanagementbackend.model.Message;
import org.kfokam48.cliniquemanagementbackend.service.MessageService;
import org.kfokam48.cliniquemanagementbackend.service.chat.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService messageService;

    @MessageMapping("/chat.send")
    public void sendMessage(Message chatMessage) {
        Message savedMessage = messageService.saveMessage(chatMessage);

        // Envoi au destinataire
        messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipientId().toString(),
                "/private",
                savedMessage
        );

        // (Optionnel) Envoi à l’expéditeur pour confirmation
        messagingTemplate.convertAndSendToUser(
                chatMessage.getSenderId().toString(),
                "/private",
                savedMessage
        );
    }
}
