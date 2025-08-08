package org.kfokam48.cliniquemanagementbackend.service;

import org.kfokam48.cliniquemanagementbackend.dto.message.MessageDTO;
import org.kfokam48.cliniquemanagementbackend.dto.message.MessageResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface MessageService {
    List<MessageResponseDTO> findBySenderAndReceiver(Long senderId, Long receiverId);
    List<MessageResponseDTO> findByReceiverAndSender(String receiverUsername, String senderUsername);
    List<MessageResponseDTO> findBySender(Long senderId);
    List<MessageResponseDTO> findByReceiver(Long receiverId);
    List<MessageResponseDTO> findAllMessages();
    MessageResponseDTO sendMessage(MessageDTO message);
    MessageResponseDTO updateMessage(Long messageId, MessageDTO message);
    ResponseEntity<String> deleteMessage(Long messageId, Long senderId);


}
