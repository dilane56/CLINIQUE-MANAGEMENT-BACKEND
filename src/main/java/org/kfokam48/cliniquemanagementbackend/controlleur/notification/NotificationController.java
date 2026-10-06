package org.kfokam48.cliniquemanagementbackend.controlleur.notification;

import lombok.RequiredArgsConstructor;
import org.kfokam48.cliniquemanagementbackend.dto.notification.NotificationResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.Notification;
import org.kfokam48.cliniquemanagementbackend.repository.AdministrateurRepository;
import org.kfokam48.cliniquemanagementbackend.service.notification.NotificationService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;
    private final AdministrateurRepository administrateurRepository;


    /**
     * Notifie un membre du personnel (Utilisateur). Ne jamais passer l'identifiant d'un patient :
     * les patients ne sont pas des utilisateurs, la notification partirait chez le membre du
     * personnel qui a le même identifiant. Pour un patient, envoyer un e-mail à Patient.email.
     */
    public void sendNotification(Long recepteurId, String title, String message, boolean sendMail) {
        Notification notif = notificationService.createnotifcation(recepteurId, title, message, sendMail);
        NotificationResponseDTO responseDTO = notificationService.convertToNotificationResponseDTO(notif);

        // envoi temps réel via STOMP
        messagingTemplate.convertAndSendToUser(
                recepteurId.toString(),
                "/notifications",
                responseDTO
        );
    }

    // Notifie tous les administrateurs (remplace l'identifiant 1L codé en dur)
    public void sendNotificationToAdmins(String title, String message) {
        administrateurRepository.findAll()
                .forEach(admin -> sendNotification(admin.getId(), title, message, false));
    }
}
