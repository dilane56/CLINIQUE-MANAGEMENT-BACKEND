package org.kfokam48.cliniquemanagementbackend.dto.utilisateur;

import lombok.Data;
import org.kfokam48.cliniquemanagementbackend.enums.UserStatus;

import java.time.LocalDateTime;

@Data
public class Contact {
    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private String role;
    private UserStatus status;
    private LocalDateTime derniereConnexion;
}
