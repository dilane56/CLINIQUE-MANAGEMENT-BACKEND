package org.kfokam48.cliniquemanagementbackend.dto.conversation;

import lombok.Data;
import org.kfokam48.cliniquemanagementbackend.dto.message.MessageResponseDTO;

@Data
public class ConversationResponseDTO {
    private Long id;
    private String participant1Email; // Email du premier participant
    private String participant2Email; // Email du deuxième participant
    private String nom; // Nom de la conversation
    private String dateCreation; // Date de création de la conversation
    private String dateDernierMessage; // Date du dernier message envoyé dans la conversation
    private MessageResponseDTO dernierMessage; // Dernier message de la conversation

}
