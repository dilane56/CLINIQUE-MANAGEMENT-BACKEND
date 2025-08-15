package org.kfokam48.cliniquemanagementbackend.controlleur;

import lombok.RequiredArgsConstructor;
import org.kfokam48.cliniquemanagementbackend.dto.message.MessageDTO;
import org.kfokam48.cliniquemanagementbackend.model.Message;
import org.kfokam48.cliniquemanagementbackend.service.chat.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(MessageDTO chatMessage) {
        Message savedMessage = chatService.sendMessages(chatMessage);

        // Envoi au destinataire
        messagingTemplate.convertAndSendToUser(
                chatMessage.getExpediteurId().toString(),
                "/private",
                savedMessage
        );

        // (Optionnel) Envoi à l’expéditeur pour confirmation
        messagingTemplate.convertAndSendToUser(
                chatMessage.getDestinataireId().toString(),
                "/private",
                savedMessage
        );
    }
}
