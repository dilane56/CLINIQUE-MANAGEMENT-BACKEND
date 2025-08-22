package org.kfokam48.cliniquemanagementbackend.dto;

import lombok.Data;

@Data
public class MailDTO {
    private String destinataireEmail;
    private String sujet;
    private String message;
}
