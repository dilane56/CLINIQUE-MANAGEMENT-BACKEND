package org.kfokam48.cliniquemanagementbackend.controlleur;

import lombok.RequiredArgsConstructor;
import org.kfokam48.cliniquemanagementbackend.dto.message.MessageResponseDTO;
import org.kfokam48.cliniquemanagementbackend.service.impl.MessageServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageServiceImpl messageService;

    @GetMapping
    public List<MessageResponseDTO> getMessagesBetweenUsers(
            @RequestParam Long user1Id,
            @RequestParam Long user2Id) {

        return messageService.getMessagesBetweenUsers(user1Id, user2Id);
    }
}

