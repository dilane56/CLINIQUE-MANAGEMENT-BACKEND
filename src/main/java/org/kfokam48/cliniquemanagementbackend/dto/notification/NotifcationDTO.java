package org.kfokam48.cliniquemanagementbackend.dto.notification;

import lombok.Data;

@Data
public class NotifcationDTO {
    private Long destinataireId;
    private String titre;
    private String message;


}
