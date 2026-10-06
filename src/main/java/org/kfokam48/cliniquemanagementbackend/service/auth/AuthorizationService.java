package org.kfokam48.cliniquemanagementbackend.service.auth;

import org.kfokam48.cliniquemanagementbackend.repository.NotificationRepository;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Règles d'accès réutilisables dans les @PreAuthorize, via "@authz".
 * Exemple : @PreAuthorize("hasRole('MEDECIN') and @authz.isCurrentUser(#medecinId)")
 */
@Component("authz")
public class AuthorizationService {

    private final UtilisateurRepository utilisateurRepository;
    private final NotificationRepository notificationRepository;

    public AuthorizationService(UtilisateurRepository utilisateurRepository, NotificationRepository notificationRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.notificationRepository = notificationRepository;
    }

    // Vrai si l'identifiant correspond à l'utilisateur authentifié (le sujet du JWT est l'email)
    public boolean isCurrentUser(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (userId == null || authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return utilisateurRepository.findByEmail(authentication.getName())
                .map(utilisateur -> userId.equals(utilisateur.getId()))
                .orElse(false);
    }

    // Vrai si l'utilisateur authentifié est le destinataire de la notification
    public boolean isNotificationRecipient(Long notificationId) {
        if (notificationId == null) {
            return false;
        }
        return notificationRepository.findById(notificationId)
                .map(notification -> isCurrentUser(notification.getDestinataireId()))
                .orElse(false);
    }
}
