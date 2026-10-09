package org.kfokam48.cliniquemanagementbackend.dto.message;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageDTO {

    @NotNull(message = "L'expéditeur est obligatoire")
    private Long expediteurId;
    @NotNull(message = "Le destinataire est obligatoire")
    private Long destinataireId;
    @NotNull(message = "Le contenu du message est obligatoire")
    private String contenu;


}
