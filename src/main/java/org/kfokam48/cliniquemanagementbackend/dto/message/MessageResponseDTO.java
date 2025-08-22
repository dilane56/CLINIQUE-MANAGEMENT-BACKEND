package org.kfokam48.cliniquemanagementbackend.dto.message;

import lombok.Data;
import org.kfokam48.cliniquemanagementbackend.enums.MessageStatus;

import java.time.Instant;


@Data
public class MessageResponseDTO {

    private Long id;
    private Long expediteurId;
    private String expediteurNom;
    private String expediteurPrenom;
    private Long destinataireId;
    private String destinataireNom;
    private String destinatairePrenom;
    private String content;
    private Instant dateEnvoi; // Recommandé pour la gestion des fuseaux horaires
    private Boolean lu;
    private MessageStatus status; // Ajout pour un statut plus précis




}