package org.kfokam48.cliniquemanagementbackend.mapper;

import org.kfokam48.cliniquemanagementbackend.dto.MessageDTO;
import org.kfokam48.cliniquemanagementbackend.dto.MessageResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.Message;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MessageMapper {
    private final UtilisateurRepository utilisateurRepository;

    public MessageMapper(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    public MessageResponseDTO messageToMessageResponseDTO (Message message){
        MessageResponseDTO messageResponseDTO = new MessageResponseDTO();
        messageResponseDTO.setSenderEmail(message.getSender().getEmail());
        messageResponseDTO.setReceiverEmail(message.getReceiver().getEmail());
        messageResponseDTO.setContent(message.getContent());
        messageResponseDTO.setDateEnvoi(message.getDateEnvoi());
        return messageResponseDTO;
    }

   public Message messageDToToMessage(MessageDTO messageDTO){
        Message message = new Message();
        message.setSender(utilisateurRepository.findByEmail(messageDTO.getSenderEmail()).orElseThrow(() -> new RuntimeException("Sender not found")));
        message.setReceiver(utilisateurRepository.findByEmail(messageDTO.getReceiverEmail()).orElseThrow(() -> new RuntimeException("Receiver not found")));
        message.setContent(messageDTO.getContent());
        return message;
   }

   public List<MessageResponseDTO> messageListToMessageResponseDTOList(List<Message> messages) {
        return messages.stream()
                .map(this::messageToMessageResponseDTO)
                .toList();
    }

    public List<Message> messageDTOListToMessageList(List<MessageDTO> messageDTOs) {
        return messageDTOs.stream()
                .map(this::messageDToToMessage)
                .toList();
    }
}
