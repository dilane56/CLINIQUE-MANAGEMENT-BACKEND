package org.kfokam48.cliniquemanagementbackend.service.impl;


import org.springframework.data.domain.Pageable;
import org.kfokam48.cliniquemanagementbackend.dto.PageResponse;
import jakarta.validation.Valid;
import org.kfokam48.cliniquemanagementbackend.service.notification.NotificationService;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousDTO;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousUpdateDto;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;
import org.kfokam48.cliniquemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.cliniquemanagementbackend.mapper.RendezVousMapper;
import org.kfokam48.cliniquemanagementbackend.model.RendezVous;
import org.kfokam48.cliniquemanagementbackend.repository.MedecinRepository;
import org.kfokam48.cliniquemanagementbackend.repository.PatientRepository;
import org.kfokam48.cliniquemanagementbackend.repository.RendezVousRepository;
import org.kfokam48.cliniquemanagementbackend.repository.SecretaireRepository;
import org.kfokam48.cliniquemanagementbackend.repository.TypeRendezVousRepository;
import org.kfokam48.cliniquemanagementbackend.service.RendezVousService;
import org.kfokam48.cliniquemanagementbackend.service.mail.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class RendezVousServiceImpl implements RendezVousService {
    // Un rendez-vous dans l'un de ces statuts ne bloque plus le créneau
    static final Set<StatutRendezVous> STATUTS_LIBERANT_LE_CRENEAU =
            EnumSet.of(StatutRendezVous.ANNULER, StatutRendezVous.A_REPROGRAMMER, StatutRendezVous.EXPIRE);
    // Les identifiants commencent à 1 : 0 n'exclut aucun rendez-vous lors d'une création
    private static final Long NOUVEAU_RENDEZ_VOUS = 0L;
    private static final DateTimeFormatter FORMAT_DATE_HEURE = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");

    private final RendezVousRepository rendezVousRepository;
    private final RendezVousMapper rendezVousMapper;
    private final PatientRepository patientRepository;
    private final MedecinRepository medecinRepository;
    private final TypeRendezVousRepository typeRendezVousRepository;
    private final SecretaireRepository secretaireRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public RendezVousServiceImpl(RendezVousRepository rendezVousRepository, RendezVousMapper rendezVousMapper, PatientRepository patientRepository, MedecinRepository medecinRepository, TypeRendezVousRepository typeRendezVousRepository, SecretaireRepository secretaireRepository, NotificationService notificationService, EmailService emailService) {
        this.rendezVousRepository = rendezVousRepository;
        this.rendezVousMapper = rendezVousMapper;
        this.patientRepository = patientRepository;
        this.medecinRepository = medecinRepository;
        this.typeRendezVousRepository = typeRendezVousRepository;
        this.secretaireRepository = secretaireRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    @Override
    public RendezVousResponseDTO save(@Valid RendezVousDTO rendezVousDTO) {
        // secretaireId reçoit des notifications : il doit désigner une secrétaire existante
        if (rendezVousDTO.getSecretaireId() != null && !secretaireRepository.existsById(rendezVousDTO.getSecretaireId())) {
            throw new IllegalArgumentException("Secrétaire introuvable : " + rendezVousDTO.getSecretaireId());
        }
        RendezVous rendezVous = new RendezVous();
        rendezVous.setDateRendezVous(rendezVousDTO.getDateRendezVous());
        rendezVous.setMotif(rendezVousDTO.getMotif());
        rendezVous.setSecretaireId(rendezVousDTO.getSecretaireId());
        affecterEtVerifierCreneau(rendezVous, rendezVousDTO.getMedecinId(), rendezVousDTO.getPatientId(),
                rendezVousDTO.getTypeRendezVousId(), NOUVEAU_RENDEZ_VOUS);

        rendezVous.setStatutRendezVous(StatutRendezVous.EN_ATTENTE);
        RendezVousResponseDTO response = rendezVousMapper.rendezVousToRendezVousResponseDto(
                rendezVousRepository.save(rendezVous));
        notificationService.sendNotification(rendezVousDTO.getMedecinId(), "Rendez-vous", "Vous avez un nouveau rendez-vous", true);
        envoyerEmailPatient(rendezVous, "Votre rendez-vous",
                "Votre rendez-vous du " + dateHeure(rendezVous) + " avec le Dr " + rendezVous.getMedecin().getNom() + " est enregistré.");
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public RendezVousResponseDTO findById(Long Id) {
        return rendezVousMapper.rendezVousToRendezVousResponseDto(
                rendezVousRepository.findById(Id)
                        .orElseThrow(() -> new RessourceNotFoundException("Rendez-vous not found"))
        );
    }

    @Override
    public RendezVousResponseDTO update(Long id, @Valid RendezVousUpdateDto rendezVousDTO) {
        RendezVous rendezVous = rendezVousRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Rendez-vous not found"));

        rendezVous.setDateRendezVous(rendezVousDTO.getDateRendezVous());
        rendezVous.setMotif(rendezVousDTO.getMotif());
        affecterEtVerifierCreneau(rendezVous, rendezVousDTO.getMedecinId(), rendezVousDTO.getPatientId(),
                rendezVousDTO.getTypeRendezVousId(), rendezVous.getId());

        rendezVous.setStatutRendezVous(StatutRendezVous.EN_ATTENTE);
        RendezVousResponseDTO response = rendezVousMapper.rendezVousToRendezVousResponseDto(
                rendezVousRepository.save(rendezVous));
        notificationService.sendNotification(rendezVousDTO.getMedecinId(), "Rendez-vous", "Le rendez-vous avec le patient " + rendezVous.getPatient().getNom() + " a été mis à jour", true);
        envoyerEmailPatient(rendezVous, "Votre rendez-vous a été modifié",
                "Votre rendez-vous est désormais prévu le " + dateHeure(rendezVous) + " avec le Dr " + rendezVous.getMedecin().getNom() + ".");
        return response;
    }

    /**
     * Les patients ne sont pas des utilisateurs : on leur écrit directement à leur adresse e-mail.
     * (Avant I20, une notification était créée avec l'identifiant du patient et partait chez le
     * membre du personnel ayant le même identifiant.)
     */
    private void envoyerEmailPatient(RendezVous rendezVous, String sujet, String message) {
        String email = rendezVous.getPatient().getEmail();
        if (email != null && !email.isBlank()) {
            emailService.sendEmail(email, sujet, message);
        }
    }

    private static String dateHeure(RendezVous rendezVous) {
        return rendezVous.getDateRendezVous().format(FORMAT_DATE_HEURE);
    }

    /**
     * Affecte médecin, patient et type au rendez-vous puis vérifie que le créneau est libre.
     *
     * Le médecin et le patient sont lus avec un verrou en écriture (SELECT ... FOR UPDATE) :
     * deux réservations simultanées pour le même médecin (ou le même patient) s'exécutent l'une
     * après l'autre, et la seconde voit le rendez-vous enregistré par la première. Le verrou est
     * relâché à la fin de la transaction (classe annotée @Transactional).
     * Ordre de verrouillage fixe (médecin puis patient) pour éviter les interblocages.
     */
    private void affecterEtVerifierCreneau(RendezVous rendezVous, Long medecinId, Long patientId,
                                           Long typeRendezVousId, Long rendezVousIdAIgnorer) {
        rendezVous.setMedecin(medecinRepository.findByIdForUpdate(medecinId)
                .orElseThrow(() -> new RessourceNotFoundException("Medecin not found")));
        rendezVous.setPatient(patientRepository.findByIdForUpdate(patientId)
                .orElseThrow(() -> new RessourceNotFoundException("Patient not found")));
        rendezVous.setTypeRendezVous(typeRendezVousRepository.findById(typeRendezVousId)
                .orElseThrow(() -> new RessourceNotFoundException("Type de rendez-vous not found")));

        LocalDateTime debut = rendezVous.getDateRendezVous();
        if (debut.isBefore(LocalDateTime.now().plusMinutes(30))) {
            throw new IllegalArgumentException("La date du rendez-vous doit être au moins 30 minutes dans le futur.");
        }
        LocalDateTime fin = debut.plusMinutes(rendezVous.getTypeRendezVous().getDuree());
        rendezVous.setDateTimeFinRendezVousPossible(fin);

        LocalDateTime debutJour = debut.toLocalDate().atStartOfDay();
        if (rendezVousRepository.existsRendezVousPatientSurPeriode(
                patientId, debutJour, debutJour.plusDays(1), rendezVousIdAIgnorer, STATUTS_LIBERANT_LE_CRENEAU)) {
            throw new IllegalArgumentException("Le patient possède déjà un rendez-vous ce jour-là.");
        }
        if (rendezVousRepository.existsChevauchementMedecin(
                medecinId, debut, fin, rendezVousIdAIgnorer, STATUTS_LIBERANT_LE_CRENEAU)) {
            throw new IllegalArgumentException("Ce créneau est déjà pris pour ce médecin.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousResponseDTO> findAll() {
        return rendezVousMapper.rendezVousListToRendezVousResponseDtoList(rendezVousRepository.findAll());
    }

    @Override
    public ResponseEntity<String> deleteById(Long id) {
        RendezVous rendezVous = rendezVousRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Rendez-vous not found"));
        rendezVousRepository.deleteById(id);
        return ResponseEntity.ok("Rendez-vous deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousResponseDTO> findByMedecinId(Long medecinId) {
        List<RendezVous> rendezVousList = rendezVousRepository.findByMedecinId(medecinId);
        return rendezVousList.stream()
                .map(rendezVousMapper::rendezVousToRendezVousResponseDto)
                .toList();
    }

    @Override
    public RendezVousResponseDTO updateStatut(Long id, StatutRendezVous statut) {
        RendezVous rendezVous = rendezVousRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Rendez-vous not found"));

        if (statut == StatutRendezVous.EN_COURS && rendezVous.getDateRendezVous().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Impossible de passer le statut à EN_COURS avant la date et l'heure du rendez-vous.");
        }

        rendezVous.setStatutRendezVous(statut);
        RendezVous updated = rendezVousRepository.save(rendezVous);

        // Guard NPE : secretaireId est nullable
        if (rendezVous.getSecretaireId() != null) {
            notificationService.sendNotification(rendezVous.getSecretaireId(), "Rendez-vous", "Le statut du rendez-vous avec le patient " + rendezVous.getPatient().getNom() + " a été mis à jour", false);
        }
        envoyerEmailPatient(rendezVous, "Statut de votre rendez-vous",
                "Le statut de votre rendez-vous du " + dateHeure(rendezVous) + " avec le Dr " + rendezVous.getMedecin().getNom()
                        + " est maintenant : " + statut + ".");
        notificationService.sendNotification(rendezVous.getMedecin().getId(), "Rendez-vous", "Le rendez-vous avec le patient " + rendezVous.getPatient().getNom() + " a changé de statut", true);
        return rendezVousMapper.rendezVousToRendezVousResponseDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousResponseDTO> findRendezVousDuJourByMedecin(Long medecinId) {
        LocalDateTime debutJour = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime finJour = debutJour.plusDays(1).minusSeconds(1);
        List<RendezVous> rendezVousList = rendezVousRepository.findByMedecinIdAndDateRendezVousBetween(medecinId, debutJour, finJour);
        return rendezVousMapper.rendezVousListToRendezVousResponseDtoList(rendezVousList);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RendezVousResponseDTO> findAll(Pageable pageable) {
        return PageResponse.of(rendezVousRepository.findAll(pageable), rendezVousMapper::rendezVousListToRendezVousResponseDtoList);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RendezVousResponseDTO> findByMedecinId(Long medecinId, Pageable pageable) {
        return PageResponse.of(rendezVousRepository.findByMedecinId(medecinId, pageable), rendezVousMapper::rendezVousListToRendezVousResponseDtoList);
    }
}
