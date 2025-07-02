package org.kfokam48.cliniquemanagementbackend.dto;

import jakarta.persistence.ManyToOne;
import lombok.Data;
import org.kfokam48.cliniquemanagementbackend.model.Utilisateur;

import java.time.LocalDateTime;

@Data
public class MessageResponseDTO {

    private String senderEmail;
    private String  receiverEmail;
    private String content;
    private LocalDateTime dateEnvoi;
}
