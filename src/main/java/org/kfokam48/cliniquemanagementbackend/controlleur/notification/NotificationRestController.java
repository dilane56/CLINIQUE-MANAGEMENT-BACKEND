package org.kfokam48.cliniquemanagementbackend.controlleur.notification;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kfokam48.cliniquemanagementbackend.dto.MailDTO;
import org.kfokam48.cliniquemanagementbackend.dto.notification.NotificationResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.Notification;
import org.kfokam48.cliniquemanagementbackend.service.mail.EmailService;
import org.kfokam48.cliniquemanagementbackend.service.notification.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationRestController {

    private final NotificationService notificationService;
    private final EmailService emailService;

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @authz.isCurrentUser(#userId)")
    public List<NotificationResponseDTO> getUserNotifications(@PathVariable Long userId) {
        return notificationService.getUserNotifications(userId);
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("@authz.isNotificationRecipient(#id)") // Seul le destinataire marque sa notification comme lue
    public void markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }

    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')") // Seul l'admin envoie un e-mail libre depuis l'adresse de la clinique
    public ResponseEntity<String> sendEmail(@Valid @RequestBody MailDTO mailDTO) {
       emailService.sendMail(mailDTO);
        return ResponseEntity.ok("mail envoyer avec succes");
    }
}
