package org.kfokam48.cliniquemanagementbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kfokam48.cliniquemanagementbackend.enums.MessageStatus;

import java.time.Instant;
import java.time.LocalDateTime;




@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Utilisateur expediteur;

    @ManyToOne
    private Utilisateur destinataire;

    private String contenu;

    private Instant dateEnvoi;

    @Enumerated(EnumType.STRING)
    private MessageStatus messageStatus;

    private Boolean lu = false;

    @ManyToOne
    private Conversation conversation;
}
