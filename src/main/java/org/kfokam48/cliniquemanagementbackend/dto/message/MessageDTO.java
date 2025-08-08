package org.kfokam48.cliniquemanagementbackend.dto.message;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageDTO {

    @NotNull(message = "Expediter ID cannot be null")
    private Long expediteurId;
    @NotNull(message = "Destinataire ID cannot be null")
    private Long destinataireId;
    @NotNull(message = "Contenu cannot be null")
    private String contenu;


}
