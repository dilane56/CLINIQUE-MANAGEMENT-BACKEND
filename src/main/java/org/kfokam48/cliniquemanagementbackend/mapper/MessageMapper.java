package org.kfokam48.cliniquemanagementbackend.mapper;

import org.kfokam48.cliniquemanagementbackend.dto.message.MessageDTO;
import org.kfokam48.cliniquemanagementbackend.dto.message.MessageResponseDTO;
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
        messageResponseDTO.setExpediteurEmail(message.getExpediteur().getEmail());
        messageResponseDTO.setExpediteurNom(message.getExpediteur().getNom());
        messageResponseDTO.setExpediteurPrenom(message.getExpediteur().getPrenom());
        messageResponseDTO.setDestinataireEmail(message.getDestinataire().getEmail());
        messageResponseDTO.setDestinataireNom(message.getDestinataire().getNom());
        messageResponseDTO.setDestinatairePrenom(message.getDestinataire().getPrenom());
        messageResponseDTO.setId(message.getId());
        messageResponseDTO.setLu(message.getLu());
        messageResponseDTO.setContent(message.getContenu());
        messageResponseDTO.setDateEnvoi(message.getDateEnvoi());
        return messageResponseDTO;
    }

   public Message messageDToToMessage(MessageDTO messageDTO){
        Message message = new Message();
        message.setExpediteur(utilisateurRepository.findById(messageDTO.getExpediteurId()).orElseThrow(() -> new RuntimeException("Sender not found")));
        message.setDestinataire(utilisateurRepository.findById(messageDTO.getDestinataireId()).orElseThrow(() -> new RuntimeException("Receiver not found")));
        message.setContenu(messageDTO.getContenu());
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
