package org.kfokam48.cliniquemanagementbackend.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageDTO {

    @NotNull(message = "Sender username cannot be null")
    private String senderEmail;
    @NotNull(message = "Receiver username cannot be null")
    private String receiverEmail;
    private String content;


}
