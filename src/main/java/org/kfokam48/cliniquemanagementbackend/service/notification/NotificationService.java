package org.kfokam48.cliniquemanagementbackend.service.notification;

import lombok.RequiredArgsConstructor;
import org.kfokam48.cliniquemanagementbackend.dto.notification.NotifcationDTO;
import org.kfokam48.cliniquemanagementbackend.dto.notification.NotificationResponseDTO;
import org.kfokam48.cliniquemanagementbackend.mapper.NotificationMapper;
import org.kfokam48.cliniquemanagementbackend.model.Notification;
import org.kfokam48.cliniquemanagementbackend.model.Utilisateur;
import org.kfokam48.cliniquemanagementbackend.repository.NotificationRepository;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.kfokam48.cliniquemanagementbackend.service.mail.EmailService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UtilisateurRepository utilisateurRepository;
    private final EmailService emailService;


    public Notification createnotifcation(Long recepteurId, String titre, String message, boolean sendMail ){
        Notification notif = Notification.builder()
                .destinataireId(recepteurId)
                .titre(titre)
                .dateEnvoi(LocalDateTime.now())
                .message(message)
                .lu(false)
                .build();

            notificationRepository.save(notif);

        // Envoi d'email
        if (sendMail) {
            Utilisateur user = utilisateurRepository.findById(recepteurId).orElse(null);
            String email = user != null ? user.getEmail() : null;
            if (email != null && !email.isEmpty()) {
                emailService.sendEmail(email, titre, message);
            }
        }

            return notif;


    }

    public NotificationResponseDTO convertToNotificationResponseDTO(Notification notification){
        return notificationMapper.notficationToNotificationResponseDTO(notification);
    }
    public List<NotificationResponseDTO> getUserNotifications(Long recepteurId) {
        return  notificationMapper.notificationListToNotificationResponseDTOList(notificationRepository.findByDestinataireIdOrderByDateEnvoiDesc(recepteurId));
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setLu(true);
            notificationRepository.save(n);
        });
    }
}
