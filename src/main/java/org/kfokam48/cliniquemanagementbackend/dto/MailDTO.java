package org.kfokam48.cliniquemanagementbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MailDTO {
    @NotBlank(message = "L'email du destinataire est obligatoire")
    @Email(message = "L'email du destinataire n'est pas valide")
    private String destinataireEmail;

    @NotBlank(message = "Le sujet est obligatoire")
    @Size(max = 200, message = "Le sujet ne doit pas dépasser 200 caractères")
    private String sujet;

    @NotBlank(message = "Le message est obligatoire")
    @Size(max = 5000, message = "Le message ne doit pas dépasser 5000 caractères")
    private String message;
}
