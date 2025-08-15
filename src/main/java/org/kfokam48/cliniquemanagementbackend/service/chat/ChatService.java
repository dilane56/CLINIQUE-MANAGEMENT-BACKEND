package org.kfokam48.cliniquemanagementbackend.service.chat;

import lombok.RequiredArgsConstructor;
import org.kfokam48.cliniquemanagementbackend.dto.message.MessageDTO;
import org.kfokam48.cliniquemanagementbackend.dto.message.MessageResponseDTO;
import org.kfokam48.cliniquemanagementbackend.mapper.MessageMapper;
import org.kfokam48.cliniquemanagementbackend.model.Conversation;
import org.kfokam48.cliniquemanagementbackend.model.Message;
import org.kfokam48.cliniquemanagementbackend.model.Utilisateur;
import org.kfokam48.cliniquemanagementbackend.repository.ConversationRepository;
import org.kfokam48.cliniquemanagementbackend.repository.MessageRepository;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepo;
    private final MessageRepository messageRepo;
    private final UtilisateurRepository userRepo;
    private final MessageMapper messageMapper;

    public MessageResponseDTO sendMessage(MessageDTO messageDTO) {
        Utilisateur sender = userRepo.findById(messageDTO.getExpediteurId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        Utilisateur receiver = userRepo.findById(messageDTO.getDestinataireId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        // Recherche d'une conversation existante entre ces 2 utilisateurs
        Conversation conversation = conversationRepo
                .findByParticipants(sender, receiver)
                .orElseGet(() -> {
                    Conversation newConv = new Conversation();
                    newConv.setParticipants(List.of(sender, receiver));
                    newConv.setCreatedAt(LocalDateTime.now());
                    return conversationRepo.save(newConv);
                });

        // Création du message
        Message message = new Message();
        message.setConversation(conversation);
        message.setExpediteur(sender);
        message.setContenu(messageDTO.getContenu());
        message.setDestinataire(receiver);
        message.setDateEnvoi(LocalDateTime.now());

        messageRepo.save(message);
        return messageMapper.messageToMessageResponseDTO(message);

    }
    public Message sendMessages(MessageDTO messageDTO){
        Utilisateur sender = userRepo.findById(messageDTO.getExpediteurId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        Utilisateur receiver = userRepo.findById(messageDTO.getDestinataireId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        // Recherche d'une conversation existante entre ces 2 utilisateurs
        Conversation conversation = conversationRepo
                .findByParticipants(sender, receiver)
                .orElseGet(() -> {
                    Conversation newConv = new Conversation();
                    newConv.setParticipants(List.of(sender, receiver));
                    newConv.setCreatedAt(LocalDateTime.now());
                    return conversationRepo.save(newConv);
                });

        // Création du message
        Message message = new Message();
        message.setConversation(conversation);
        message.setExpediteur(sender);
        message.setContenu(messageDTO.getContenu());
        message.setDestinataire(receiver);
        message.setDateEnvoi(LocalDateTime.now());

        return messageRepo.save(message);
    }
}
