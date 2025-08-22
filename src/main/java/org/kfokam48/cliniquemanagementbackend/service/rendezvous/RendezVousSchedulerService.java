package org.kfokam48.cliniquemanagementbackend.service.rendezvous;

import lombok.RequiredArgsConstructor;
import org.kfokam48.cliniquemanagementbackend.controlleur.notification.NotificationController;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;
import org.kfokam48.cliniquemanagementbackend.model.RendezVous;
import org.kfokam48.cliniquemanagementbackend.repository.RendezVousRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RendezVousSchedulerService {

    private final RendezVousRepository rendezVousRepository;
    private final NotificationController notificationController;

    // Cette méthode s'exécute toutes les heures
    @Scheduled(cron = "0 0 * * * *") // Toutes les heures à HH:00
    public void verifierEtExpirerRendezVousNonConfirmes() {
        List<RendezVous> rendezVousNonConfirme = rendezVousRepository.findAllByStatutRendezVous(StatutRendezVous.EN_ATTENTE);
        for (RendezVous rdv : rendezVousNonConfirme) {
            if (rdv.getDateRendezVous().isBefore(LocalDateTime.now())) {
                rdv.setStatutRendezVous(StatutRendezVous.EXPIRE);
                rendezVousRepository.save(rdv);
                notificationController.sendNotification(rdv.getMedecin().getId(), "Rendez-vous expiré", "Le rendez-vous de " + rdv.getPatient().getNom() + " a été expiré.",true);
                notificationController.sendNotification(rdv.getSecretaireId(), "Rendez-vous expiré", "Le rendez-vous de " + rdv.getPatient().getNom() + " a été expiré.",false);
                System.out.println("Rendez-vous expiré : " + rdv.getId());
            }
        }
    }
    @Scheduled(cron = "0 0 * * * *") // Toutes les heures à HH:00
    public void verfierRendezVousConfirmeMaisPasDebuter(){
        List<RendezVous> rendezVousNotStart = rendezVousRepository.findAllByStatutRendezVous(StatutRendezVous.CONFIRME);
        for (RendezVous rdv : rendezVousNotStart) {
            if (rdv.getDateTimeFinRendezVousPossible().isBefore(LocalDateTime.now())) {
                rdv.setStatutRendezVous(StatutRendezVous.A_REPROGRAMMER);
                rendezVousRepository.save(rdv);
                notificationController.sendNotification(rdv.getMedecin().getId(), "Rendez-vous à reprogrammer", "Le rendez-vous de " + rdv.getPatient().getNom() + " doit être reprogrammé.", true);
                notificationController.sendNotification(rdv.getSecretaireId(), "Rendez-vous à reprogrammer", "Le rendez-vous de " + rdv.getPatient().getNom() + " doit être reprogrammé.", false);
                System.out.println("Rendez-vous a reprogrammer : " + rdv.getId());
            }
        }
    }


    @Scheduled(cron = "0 0 * * * *") // Toutes les heures à HH:00
    public void verfierRendezVousDebuterMaisPasterminer(){
        List<RendezVous> rendezVousNotStart = rendezVousRepository.findAllByStatutRendezVous(StatutRendezVous.EN_COURS);
        for (RendezVous rdv : rendezVousNotStart) {
            if (rdv.getDateTimeFinRendezVousPossible().isBefore(LocalDateTime.now())) {
                rdv.setStatutRendezVous(StatutRendezVous.TERMINE);
                rendezVousRepository.save(rdv);
                notificationController.sendNotification(rdv.getMedecin().getId(), "Rendez-vous Terminer", "Le rendez-vous de " + rdv.getPatient().getNom() + " est terminé.",false);
                notificationController.sendNotification(rdv.getSecretaireId(), "Rendez-vous Terminer", "Le rendez-vous de " + rdv.getPatient().getNom() + " est terminé.",false);
                System.out.println("Rendez-vous Terminer : " + rdv.getId());
            }
        }
    }
}

