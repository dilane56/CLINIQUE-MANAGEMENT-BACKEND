package org.kfokam48.cliniquemanagementbackend.dto.message;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageResponseDTO {

    private Long id;
    private String expediteurEmail;
    private String expediteurNom;
    private String expediteurPrenom;
    private String  destinataireEmail;
    private String destinataireNom;
    private String destinatairePrenom;
    private String content;
    private LocalDateTime dateEnvoi;
    private Boolean lu;
}
