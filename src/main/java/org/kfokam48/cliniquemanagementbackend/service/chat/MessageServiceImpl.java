package org.kfokam48.cliniquemanagementbackend.service.chat;

import jakarta.validation.Valid;
import org.kfokam48.cliniquemanagementbackend.dto.message.MessageDTO;
import org.kfokam48.cliniquemanagementbackend.dto.message.MessageResponseDTO;
import org.kfokam48.cliniquemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.cliniquemanagementbackend.mapper.MessageMapper;
import org.kfokam48.cliniquemanagementbackend.model.Message;
import org.kfokam48.cliniquemanagementbackend.repository.MessageRepository;
import org.kfokam48.cliniquemanagementbackend.service.chat.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {
    private final MessageMapper messageMapper;
    private final MessageRepository messageRepository;

    public MessageServiceImpl(MessageMapper messageMapper, MessageRepository messageRepository) {
        this.messageMapper = messageMapper;
        this.messageRepository = messageRepository;
    }

    @Override
    public List<MessageResponseDTO> findBySenderAndReceiver(Long senderId, Long receiverId) {
        return messageMapper.messageListToMessageResponseDTOList(messageRepository.findBySenderAndReceiver(senderId, receiverId));
    }

    @Override
    public List<MessageResponseDTO> findByReceiverAndSender(String receiverUsername, String senderUsername) {
        return messageMapper.messageListToMessageResponseDTOList(messageRepository.findByReceiverAndSender(receiverUsername, senderUsername));
    }

    @Override
    public List<MessageResponseDTO> findBySender(Long senderId) {
        return messageMapper.messageListToMessageResponseDTOList(messageRepository.findBySender(senderId));
    }

    @Override
    public List<MessageResponseDTO> findByReceiver(Long receiverId) {
        return messageMapper.messageListToMessageResponseDTOList(messageRepository.findByReceiver(receiverId));
    }

    @Override
    public List<MessageResponseDTO> findAllMessages() {
        return messageMapper.messageListToMessageResponseDTOList(messageRepository.findAll());
    }

    @Override
    public MessageResponseDTO sendMessage(@Valid MessageDTO messageDTO) {
        Message message = messageMapper.messageDToToMessage(messageDTO);
        message.setDateEnvoi(LocalDateTime.now());
        messageRepository.save(message);
        return messageMapper.messageToMessageResponseDTO(message);

    }

    @Override
    public MessageResponseDTO updateMessage(Long messageId,@Valid MessageDTO message) {
        return null;
    }

    @Override
    public ResponseEntity<String> deleteMessage(Long messageId, Long senderId) {

        Message message = messageRepository.findById(messageId).orElseThrow(() -> new RessourceNotFoundException("Message not found"));
        messageRepository.delete(message);
        if (!Objects.equals(message.getExpediteur().getId(), senderId)) {
            return new ResponseEntity<String>("You are not authorized to delete this message", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<String>("Message deleted successfully", HttpStatus.OK);
    }

    @Override
    public List<MessageResponseDTO> getMessagesBetweenUsers(Long userId1, Long userId2) {
        return messageMapper.messageListToMessageResponseDTOList(messageRepository.findConversation(userId1, userId2));
    }

    @Override
    public List<MessageResponseDTO> getMessages(Long userId1, Long userId2) {
        return messageMapper.messageListToMessageResponseDTOList(
                messageRepository.findByExpediteurIdAndDestinataireIdOrExpediteurIdAndDestinataireIdOrderByDateEnvoi(
                        userId1, userId2, userId2, userId1));
    }

}
