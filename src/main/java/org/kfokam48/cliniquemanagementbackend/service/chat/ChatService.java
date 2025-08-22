package org.kfokam48.cliniquemanagementbackend.service.chat;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kfokam48.cliniquemanagementbackend.dto.message.MessageDTO;
import org.kfokam48.cliniquemanagementbackend.dto.message.MessageResponseDTO;
import org.kfokam48.cliniquemanagementbackend.enums.MessageStatus;
import org.kfokam48.cliniquemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.cliniquemanagementbackend.mapper.MessageMapper;
import org.kfokam48.cliniquemanagementbackend.model.Conversation;
import org.kfokam48.cliniquemanagementbackend.model.Message;
import org.kfokam48.cliniquemanagementbackend.model.Utilisateur;
import org.kfokam48.cliniquemanagementbackend.repository.ConversationRepository;
import org.kfokam48.cliniquemanagementbackend.repository.MessageRepository;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepo;
    private final MessageRepository messageRepo;
    private final UtilisateurRepository userRepo;
    private final MessageMapper messageMapper;

    @Transactional
    public Message sendMessages(@Valid MessageDTO messageDTO){
        Utilisateur sender = userRepo.findById(messageDTO.getExpediteurId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        Utilisateur receiver = userRepo.findById(messageDTO.getDestinataireId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Conversation conversation = conversationRepo
                .findByParticipants(sender, receiver)
                .orElseGet(() -> {
                    Conversation newConv = new Conversation();
                    newConv.setParticipants(List.of(sender, receiver));
                    newConv.setCreatedAt(Instant.now());
                    return conversationRepo.save(newConv);
                });

        Message message = new Message();
        message.setConversation(conversation);
        message.setExpediteur(sender);
        message.setContenu(messageDTO.getContenu());
        message.setDestinataire(receiver);
        message.setDateEnvoi(Instant.now());
        message.setMessageStatus(MessageStatus.SENT);
        message.setLu(false); // Initialisation à 'false' par défaut

        return messageRepo.save(message);
    }

    // Convertit une entité Message en DTO
    public MessageResponseDTO convertToResponseDTO(Message message) {
        return messageMapper.messageToMessageResponseDTO(message);
    }

    // Récupère l'historique de la conversation entre deux utilisateurs
    public List<MessageResponseDTO> getConversation(Long user1Id, Long user2Id) {
        List<Message> messages = messageRepo.findConversation(user1Id, user2Id);
        return messages.stream()
                .map(message -> {
                    // Marquer les messages comme 'DELIVERED' pour le destinataire lorsqu'ils sont récupérés
                    if (message.getDestinataire().getId().equals(user1Id) && message.getMessageStatus() == MessageStatus.SENT) {
                        message.setMessageStatus(MessageStatus.DELIVERED);
                        messageRepo.save(message); // Sauvegarder l'entité mise à jour
                    }
                    return messageMapper.messageToMessageResponseDTO(message);
                })
                .collect(Collectors.toList());
    }

    // Met à jour le statut des messages d'un expéditeur donné vers un destinataire
    @Transactional
    public void markMessagesAsRead(Long senderId, Long recipientId) {
        messageRepo.updateMessageStatusToRead(senderId, recipientId);
    }

    // Récupère les conversations d'un utilisateur
    public List<Conversation> getUserConversations(Long userId) {
        Utilisateur user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return conversationRepo.findByParticipantsContaining(user);
    }

    public void deleteMessage (Long id){
        Message message = messageRepo.findById(id).orElseThrow(()->new RessourceNotFoundException("message not found"));
        messageRepo.deleteById(id);

    }
}