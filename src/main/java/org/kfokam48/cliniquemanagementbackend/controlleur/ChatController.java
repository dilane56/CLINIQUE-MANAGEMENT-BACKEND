package org.kfokam48.cliniquemanagementbackend.controlleur;

import org.kfokam48.cliniquemanagementbackend.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;


@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    @SendTo("/topic/messages")
    public Message envoyerMessage(Message message) {
        message.setDateEnvoi(LocalDateTime.now());
        // Sauvegarder dans la base
        return message;
    }

    @PostMapping("/api/messages")
    public ResponseEntity<Message> sauvegarderMessage(@RequestBody Message message) {
        // Save en base sans WebSocket (optionnel)
        return ResponseEntity.ok(message);
    }
}
