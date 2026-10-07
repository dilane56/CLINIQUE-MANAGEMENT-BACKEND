package org.kfokam48.cliniquemanagementbackend.service.auth;

import org.kfokam48.cliniquemanagementbackend.repository.FactureRepository;
import org.kfokam48.cliniquemanagementbackend.repository.LigneFactureRepository;
import org.kfokam48.cliniquemanagementbackend.repository.LignePrescriptionRepository;
import org.kfokam48.cliniquemanagementbackend.repository.NotificationRepository;
import org.kfokam48.cliniquemanagementbackend.repository.PrescriptionRepository;
import org.kfokam48.cliniquemanagementbackend.repository.RendezVousRepository;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * Règles d'accès réutilisables dans les @PreAuthorize, via "@authz".
 * Exemple : @PreAuthorize("hasRole('MEDECIN') and @authz.isCurrentUser(#medecinId)")
 *
 * Les méthodes "owns..." vérifient qu'une ressource est rattachée (via son rendez-vous)
 * au médecin connecté. Une ressource inexistante est considérée comme non possédée (403).
 */
@Component("authz")
public class AuthorizationService {

    private final UtilisateurRepository utilisateurRepository;
    private final NotificationRepository notificationRepository;
    private final RendezVousRepository rendezVousRepository;
    private final FactureRepository factureRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final LignePrescriptionRepository lignePrescriptionRepository;
    private final LigneFactureRepository ligneFactureRepository;

    public AuthorizationService(UtilisateurRepository utilisateurRepository,
                                NotificationRepository notificationRepository,
                                RendezVousRepository rendezVousRepository,
                                FactureRepository factureRepository,
                                PrescriptionRepository prescriptionRepository,
                                LignePrescriptionRepository lignePrescriptionRepository,
                                LigneFactureRepository ligneFactureRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.notificationRepository = notificationRepository;
        this.rendezVousRepository = rendezVousRepository;
        this.factureRepository = factureRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.lignePrescriptionRepository = lignePrescriptionRepository;
        this.ligneFactureRepository = ligneFactureRepository;
    }

    // Vrai si l'identifiant correspond à l'utilisateur authentifié (le sujet du JWT est l'email)
    public boolean isCurrentUser(Long userId) {
        return userId != null && currentUserId().map(userId::equals).orElse(false);
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

    public boolean ownsRendezVous(Long rendezVousId) {
        return ownedByCurrentUser(rendezVousId, rendezVousRepository::existsByIdAndMedecin_Id);
    }

    public boolean ownsFacture(Long factureId) {
        return ownedByCurrentUser(factureId, factureRepository::existsByIdAndRendezVous_Medecin_Id);
    }

    public boolean ownsPrescription(Long prescriptionId) {
        return ownedByCurrentUser(prescriptionId, prescriptionRepository::existsByIdAndRendezVous_Medecin_Id);
    }

    public boolean ownsLignePrescription(Long ligneId) {
        return ownedByCurrentUser(ligneId, lignePrescriptionRepository::existsByIdAndPrescription_RendezVous_Medecin_Id);
    }

    public boolean ownsLigneFacture(Long ligneId) {
        return ownedByCurrentUser(ligneId, ligneFactureRepository::existsByIdAndFacture_RendezVous_Medecin_Id);
    }

    private boolean ownedByCurrentUser(Long resourceId, BiPredicate<Long, Long> existsByIdAndMedecinId) {
        if (resourceId == null) {
            return false;
        }
        return currentUserId()
                .map(medecinId -> existsByIdAndMedecinId.test(resourceId, medecinId))
                .orElse(false);
    }

    private Optional<Long> currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        return utilisateurRepository.findByEmail(authentication.getName())
                .map(utilisateur -> utilisateur.getId());
    }
}
