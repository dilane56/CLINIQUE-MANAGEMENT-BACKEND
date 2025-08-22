package org.kfokam48.cliniquemanagementbackend.dto.notification;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponseDTO {
    private Long id;
    private String titre;
    private String message;
    private Boolean lu;
    private LocalDateTime dateEnvoi;

}
