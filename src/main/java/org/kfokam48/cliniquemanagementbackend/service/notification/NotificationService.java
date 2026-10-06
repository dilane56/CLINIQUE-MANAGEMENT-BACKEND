package org.kfokam48.cliniquemanagementbackend.service.notification;

import org.springframework.data.domain.Pageable;
import org.kfokam48.cliniquemanagementbackend.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.kfokam48.cliniquemanagementbackend.dto.notification.NotifcationDTO;
import org.kfokam48.cliniquemanagementbackend.dto.notification.NotificationResponseDTO;
import org.kfokam48.cliniquemanagementbackend.mapper.NotificationMapper;
import org.kfokam48.cliniquemanagementbackend.model.Notification;
import org.kfokam48.cliniquemanagementbackend.model.Utilisateur;
import org.kfokam48.cliniquemanagementbackend.repository.AdministrateurRepository;
import org.kfokam48.cliniquemanagementbackend.repository.NotificationRepository;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.kfokam48.cliniquemanagementbackend.service.mail.EmailService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;
    private final AdministrateurRepository administrateurRepository;

    /**
     * Notifie un membre du personnel (Utilisateur) : enregistrement, envoi temps réel (STOMP)
     * et e-mail optionnel. Ne jamais passer l'identifiant d'un patient : les patients ne sont pas
     * des utilisateurs, la notification partirait chez le membre du personnel qui a le même
     * identifiant. Pour un patient, envoyer un e-mail à Patient.email.
     * (Anciennement NotificationService, déplacé ici : I12.)
     */
    public void sendNotification(Long recepteurId, String title, String message, boolean sendMail) {
        Notification notif = createnotifcation(recepteurId, title, message, sendMail);
        messagingTemplate.convertAndSendToUser(
                recepteurId.toString(),
                "/notifications",
                convertToNotificationResponseDTO(notif)
        );
    }

    // Notifie tous les administrateurs
    public void sendNotificationToAdmins(String title, String message) {
        administrateurRepository.findAll()
                .forEach(admin -> sendNotification(admin.getId(), title, message, false));
    }


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

    public PageResponse<NotificationResponseDTO> getUserNotifications(Long recepteurId, Pageable pageable) {
        return PageResponse.of(notificationRepository.findByDestinataireId(recepteurId, pageable),
                notificationMapper::notificationListToNotificationResponseDTOList);
    }
}
