package org.kfokam48.cliniquemanagementbackend.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.dto.AdministrateurDTO;
import org.kfokam48.cliniquemanagementbackend.dto.lignefacture.LigneFactureDTO;
import org.kfokam48.cliniquemanagementbackend.dto.ligneprescription.LignePrescriptionDTO;
import org.kfokam48.cliniquemanagementbackend.dto.ligneprescription.LignePrescriptionUpdateDTO;
import org.kfokam48.cliniquemanagementbackend.dto.medecin.MedecinDTO;
import org.kfokam48.cliniquemanagementbackend.dto.patient.PatientDTO;
import org.kfokam48.cliniquemanagementbackend.dto.prescription.PrescriptionDTO;
import org.kfokam48.cliniquemanagementbackend.dto.prescription.PrescriptionUpdateDTO;
import org.kfokam48.cliniquemanagementbackend.dto.secretaire.SecretaireDTO;
import org.kfokam48.cliniquemanagementbackend.dto.typeRendezVous.TypeRendezVousDTO;
import org.kfokam48.cliniquemanagementbackend.enums.*;
import org.kfokam48.cliniquemanagementbackend.model.*;
import org.kfokam48.cliniquemanagementbackend.repository.RendezVousRepository;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Non-régression des mappers : la sortie de chaque méthode de mapper utilisée par l'application,
 * appliquée à des objets entièrement remplis, doit rester identique à l'instantané
 * src/test/resources/mappers/instantane.json (donc les réponses de l'API aussi).
 * <p>
 * Pour régénérer l'instantané après un changement voulu :
 * {@code ./mvnw test -Dtest=MappersSnapshotTest -Dmappers.instantane=ecrire}, puis relire le diff.
 */
@SpringJUnitConfig(MappersSnapshotTest.Config.class)
class MappersSnapshotTest {

    private static final Path INSTANTANE = Path.of("src/test/resources/mappers/instantane.json");
    private static final String PAQUET_MODELE = Utilisateur.class.getPackageName();

    @Configuration
    @ComponentScan(basePackageClasses = RendezVousMapper.class)
    static class Config {
    }

    @MockitoBean private UtilisateurRepository utilisateurRepository;
    @MockitoBean private RendezVousRepository rendezVousRepository;

    @Autowired private AdministrateurMapper administrateurMapper;
    @Autowired private FactureMapper factureMapper;
    @Autowired private LigneFactureMapper ligneFactureMapper;
    @Autowired private LignePrescriptionMapper lignePrescriptionMapper;
    @Autowired private MedecinMapper medecinMapper;
    @Autowired private MessageMapper messageMapper;
    @Autowired private NotificationMapper notificationMapper;
    @Autowired private PatientMapper patientMapper;
    @Autowired private PrescriptionMapper prescriptionMapper;
    @Autowired private RendezVousMapper rendezVousMapper;
    @Autowired private SecretaireMapper secretaireMapper;
    @Autowired private TypeRendezVousMapper typeRendezVousMapper;
    @Autowired private UtilisateurMapper utilisateurMapper;

    // ---------------------------------------------------------------- jeu de données

    private static <T extends Utilisateur> T remplir(T utilisateur, long id, String prenom, String nom, Roles role) {
        utilisateur.setId(id);
        utilisateur.setEmail(prenom.toLowerCase() + "@clinique.cm");
        utilisateur.setPrenom(prenom);
        utilisateur.setNom(nom);
        utilisateur.setPassword("$2a$10$hash");
        utilisateur.setTelephone("+237 690 00 00 0" + id);
        utilisateur.setStatus(UserStatus.EN_LIGNE);
        utilisateur.setDerniereConnexion(Instant.parse("2030-01-14T08:00:00Z"));
        utilisateur.setRole(role);
        utilisateur.setDateCreation(LocalDate.of(2029, 6, 1));
        utilisateur.setAdresse("Yaoundé, Bastos");
        return utilisateur;
    }

    private Secretaire secretaire() {
        return remplir(new Secretaire(), 4L, "Brigitte", "Ewondo", Roles.SECRETAIRE);
    }

    private Medecin medecin() {
        Medecin medecin = remplir(new Medecin(), 7L, "Paul", "Martin", Roles.MEDECIN);
        medecin.setSpecialite("Cardiologie");
        medecin.setSecretaires(new LinkedHashSet<>(List.of(secretaire())));
        return medecin;
    }

    private Patient patient() {
        Patient patient = new Patient();
        patient.setId(12L);
        patient.setEmail("aicha@mail.cm");
        patient.setNom("Ngono");
        patient.setPrenom("Aïcha");
        patient.setTelephone("+237 677 11 22 33");
        patient.setDateNaissance(LocalDate.of(1990, 3, 8));
        patient.setAntecedents("Asthme");
        patient.setAllergies("Pénicilline");
        patient.setSexe(Sexe.FEMME);
        patient.setAdresse("Douala, Akwa");
        return patient;
    }

    private TypeRendezVous type() {
        return new TypeRendezVous(3L, "Consultation", 30, new BigDecimal("15000.00"));
    }

    private RendezVous rendezVous() {
        RendezVous rendezVous = new RendezVous();
        rendezVous.setId(20L);
        rendezVous.setDateRendezVous(LocalDateTime.of(2030, 1, 15, 9, 0));
        rendezVous.setDateTimeFinRendezVousPossible(LocalDateTime.of(2030, 1, 15, 9, 30));
        rendezVous.setMotif("Douleurs thoraciques");
        rendezVous.setStatutRendezVous(StatutRendezVous.TERMINE);
        rendezVous.setSecretaireId(4L);
        rendezVous.setMedecin(medecin());
        rendezVous.setPatient(patient());
        rendezVous.setTypeRendezVous(type());
        return rendezVous;
    }

    private Prescription prescription() {
        Prescription prescription = new Prescription();
        prescription.setId(30L);
        prescription.setDescription("Traitement de 10 jours");
        prescription.setDate(LocalDate.of(2030, 1, 15));
        prescription.setRendezVous(rendezVous());
        LignePrescription ligne = new LignePrescription(31L, "Paracétamol", "500 mg", "3 fois par jour", 10, prescription);
        prescription.setLignes(new ArrayList<>(List.of(ligne)));
        return prescription;
    }

    private Facture facture() {
        Facture facture = new Facture();
        facture.setId(40L);
        facture.setMontantTotal(new BigDecimal("25000.00"));
        facture.setMontantPayement(new BigDecimal("10000.00"));
        facture.setMontantRestant(new BigDecimal("15000.00"));
        facture.setDateEmission(LocalDateTime.of(2030, 1, 15, 10, 0));
        facture.setDatePayement(LocalDateTime.of(2030, 1, 16, 11, 0));
        facture.setStatut(StatutFacture.PARTIELLEMENT_PAYE);
        facture.setRendezVous(rendezVous());
        LigneFacture ligne = new LigneFacture();
        ligne.setId(41L);
        ligne.setServiceName("Consultation");
        ligne.setQuantite(1);
        ligne.setPrixUnitaire(new BigDecimal("25000.00"));
        ligne.setPrixTotal(new BigDecimal("25000.00"));
        ligne.setFacture(facture);
        facture.setLignes(new ArrayList<>(List.of(ligne)));
        return facture;
    }

    private static <T extends org.kfokam48.cliniquemanagementbackend.dto.utilisateur.UtilisateurDTO> T compte(T dto, String prenom, Roles role) {
        dto.setEmail(prenom.toLowerCase() + "@clinique.cm");
        dto.setPassword("MotDePasse123");
        dto.setNom("Nom" + prenom);
        dto.setPrenom(prenom);
        dto.setTelephone("+237 699 99 99 99");
        dto.setRole(role);
        return dto;
    }

    // ---------------------------------------------------------------- appels

    private Map<String, Object> sorties() {
        Map<String, Object> sorties = new TreeMap<>();

        sorties.put("administrateur.dtoVersEntite", administrateurMapper.administrateurDtoToAdministrateur(compte(new AdministrateurDTO(), "Admin", Roles.ADMIN)));

        sorties.put("facture.reponse", factureMapper.factureToFactureResponseDto(facture()));
        sorties.put("facture.reponseListe", factureMapper.factureListToFactureResponseDtoList(List.of(facture())));

        LigneFactureDTO ligneFactureDTO = new LigneFactureDTO();
        ligneFactureDTO.setServiceName("Radiographie");
        ligneFactureDTO.setQuantite(2);
        ligneFactureDTO.setPrixUnitaire(new BigDecimal("12500.50"));
        sorties.put("ligneFacture.dtoVersEntite", ligneFactureMapper.ligneFactureDTOToLigneFacture(ligneFactureDTO));
        sorties.put("ligneFacture.reponse", ligneFactureMapper.toResponseDTO(facture().getLignes().get(0)));
        sorties.put("ligneFacture.reponseListe", ligneFactureMapper.toResponseDTOList(facture().getLignes()));

        LignePrescriptionDTO lignePrescriptionDTO = new LignePrescriptionDTO();
        lignePrescriptionDTO.setMedicament("Amoxicilline");
        lignePrescriptionDTO.setDosage("1 g");
        lignePrescriptionDTO.setFrequence("2 fois par jour");
        lignePrescriptionDTO.setDuree(7);
        LignePrescriptionUpdateDTO lignePrescriptionUpdateDTO = new LignePrescriptionUpdateDTO();
        lignePrescriptionUpdateDTO.setMedicament("Ibuprofène");
        lignePrescriptionUpdateDTO.setDosage("400 mg");
        lignePrescriptionUpdateDTO.setFrequence("Le soir");
        lignePrescriptionUpdateDTO.setDuree(5);
        lignePrescriptionUpdateDTO.setPrescriptionId(99L);
        sorties.put("lignePrescription.reponse", lignePrescriptionMapper.lignePrescriptionToLignePrescriptionResponseDTO(prescription().getLignes().get(0)));
        sorties.put("lignePrescription.reponseListe", lignePrescriptionMapper.lignePrescriptionsToLignePrescriptionResponseDTOs(prescription().getLignes()));
        sorties.put("lignePrescription.reponseListeNull", lignePrescriptionMapper.lignePrescriptionsToLignePrescriptionResponseDTOs(null));
        sorties.put("lignePrescription.dtoVersEntiteListe", lignePrescriptionMapper.lignePrescriptionDTOListToLignePrescriptionList(List.of(lignePrescriptionDTO)));
        sorties.put("lignePrescription.dtoVersEntiteListeNull", lignePrescriptionMapper.lignePrescriptionDTOListToLignePrescriptionList(null));
        sorties.put("lignePrescription.updateVersEntiteListe", lignePrescriptionMapper.lignePrescriptionUpdateDTOListToLignePrescriptionList(List.of(lignePrescriptionUpdateDTO)));
        sorties.put("lignePrescription.updateVersEntiteListeNull", lignePrescriptionMapper.lignePrescriptionUpdateDTOListToLignePrescriptionList(null));

        MedecinDTO medecinDTO = compte(new MedecinDTO(), "Anne", Roles.MEDECIN);
        medecinDTO.setSpecialite("Pédiatrie");
        Medecin medecinAvecRendezVous = medecin();
        medecinAvecRendezVous.setRendezvous(new ArrayList<>(List.of(rendezVous())));
        sorties.put("medecin.dtoVersEntite", medecinMapper.medecinDtoToMedecin(medecinDTO));
        sorties.put("medecin.reponse", medecinMapper.medecinToMedecinResponseDto(medecinAvecRendezVous));
        sorties.put("medecin.reponseListe", medecinMapper.medecinListToMedecinResponseDtoList(List.of(medecinAvecRendezVous)));

        Message message = new Message();
        message.setId(50L);
        message.setExpediteur(medecin());
        message.setDestinataire(secretaire());
        message.setContenu("Bonjour, le patient est arrivé ?");
        message.setDateEnvoi(Instant.parse("2030-01-15T08:55:00Z"));
        message.setMessageStatus(MessageStatus.DELIVERED);
        message.setLu(true);
        sorties.put("message.reponse", messageMapper.messageToMessageResponseDTO(message));

        Notification notification = new Notification(60L, 7L, "Nouveau rendez-vous", "Aïcha Ngono à 9 h", true, LocalDateTime.of(2030, 1, 14, 18, 0));
        sorties.put("notification.reponse", notificationMapper.notficationToNotificationResponseDTO(notification));
        sorties.put("notification.reponseListe", notificationMapper.notificationListToNotificationResponseDTOList(List.of(notification)));

        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setEmail("jean@mail.cm");
        patientDTO.setNom("Fotso");
        patientDTO.setPrenom("Jean");
        patientDTO.setTelephone("+237 655 44 33 22");
        patientDTO.setDateNaissance(LocalDate.of(1985, 11, 2));
        patientDTO.setAntecedents("Hypertension");
        patientDTO.setAllergies("Aucune");
        patientDTO.setSexe(Sexe.HOMME);
        patientDTO.setAdresse("Bafoussam");
        Patient patientAvecRendezVous = patient();
        patientAvecRendezVous.setRendezvous(new ArrayList<>(List.of(rendezVous())));
        sorties.put("patient.dtoVersEntite", patientMapper.patientDtoToPatient(patientDTO));
        sorties.put("patient.reponse", patientMapper.patientToPatientResponseDTO(patientAvecRendezVous));
        sorties.put("patient.reponseListe", patientMapper.patientListToPatientResponseDtoList(List.of(patientAvecRendezVous)));

        when(rendezVousRepository.findById(20L)).thenReturn(Optional.of(rendezVous()));
        PrescriptionDTO prescriptionDTO = new PrescriptionDTO();
        prescriptionDTO.setRendezVousId(20L);
        prescriptionDTO.setDescription("Antibiotiques");
        prescriptionDTO.setLignes(List.of(lignePrescriptionDTO));
        sorties.put("prescription.dtoVersEntite", prescriptionMapper.prescriptionDtoToPrescription(prescriptionDTO));
        sorties.put("prescription.reponse", prescriptionMapper.prescriptionToPrescriptionResponseDto(prescription()));
        sorties.put("prescription.reponseListe", prescriptionMapper.prescriptionListToPrescriptionResponseDtoList(List.of(prescription())));
        Prescription aModifier = prescription();
        PrescriptionUpdateDTO prescriptionUpdateDTO = new PrescriptionUpdateDTO();
        prescriptionUpdateDTO.setDescription("Traitement prolongé");
        prescriptionUpdateDTO.setLignes(List.of(lignePrescriptionUpdateDTO));
        prescriptionMapper.updatePrescriptionFromUpdateDTO(aModifier, prescriptionUpdateDTO, lignePrescriptionMapper);
        sorties.put("prescription.miseAJour", aModifier);

        sorties.put("rendezVous.reponse", rendezVousMapper.rendezVousToRendezVousResponseDto(rendezVous()));
        sorties.put("rendezVous.reponseListe", rendezVousMapper.rendezVousListToRendezVousResponseDtoList(List.of(rendezVous())));
        sorties.put("rendezVous.dansUtilisateurListe", rendezVousMapper.rendezVousListToRendezVousInUserDtoList(List.of(rendezVous())));

        Secretaire secretaireAvecMedecins = secretaire();
        Medecin medecinSansSecretaire = remplir(new Medecin(), 8L, "Anne", "Durand", Roles.MEDECIN);
        medecinSansSecretaire.setSpecialite("Pédiatrie");
        secretaireAvecMedecins.setMedecins(new LinkedHashSet<>(List.of(medecinSansSecretaire)));
        sorties.put("secretaire.dtoVersEntite", secretaireMapper.secretaireDtoToSecretaire(compte(new SecretaireDTO(), "Brigitte", Roles.SECRETAIRE)));
        sorties.put("secretaire.reponse", secretaireMapper.secretaireToSecretaireResponseDto(secretaireAvecMedecins));
        sorties.put("secretaire.reponseListe", secretaireMapper.secretaireListToSecretaireResponseDtoList(List.of(secretaireAvecMedecins)));

        TypeRendezVousDTO typeDTO = new TypeRendezVousDTO();
        typeDTO.setLibelle("Vaccination");
        typeDTO.setDuree(15);
        typeDTO.setTarif(new BigDecimal("10000.00"));
        sorties.put("typeRendezVous.dtoVersEntite", typeRendezVousMapper.typeRendezVousDtoToTypeRendezVous(typeDTO));
        sorties.put("typeRendezVous.reponse", typeRendezVousMapper.typeRendezVousToTypeRendezVousResponseDTO(type()));
        sorties.put("typeRendezVous.reponseListe", typeRendezVousMapper.typeRendezVousListToTypeRendezVousResponseDTOList(List.of(type())));

        // En base, les utilisateurs sont toujours des médecins, secrétaires ou administrateurs
        List<Utilisateur> utilisateurs = List.of(medecin(), secretaire(), remplir(new Administrateur(), 1L, "Admin", "Principal", Roles.ADMIN));
        sorties.put("utilisateur.reponse", utilisateurMapper.utilisateurToUtilisateurResponseDTO(medecin()));
        sorties.put("utilisateur.reponseListe", utilisateurMapper.utilisateursToUtilisateurResponseDTOs(utilisateurs));
        sorties.put("utilisateur.contacts", utilisateurMapper.utilisateursToContacts(utilisateurs));
        sorties.put("utilisateur.userDTO", utilisateurMapper.utilisateurToUserDTO(medecin()));
        return sorties;
    }

    // ---------------------------------------------------------------- comparaison

    @Test
    void mappersOutputMatchesTheSnapshot() throws Exception {
        Map<String, Object> decrit = new TreeMap<>();
        sorties().forEach((cle, valeur) -> decrit.put(cle, decrire(valeur, 0)));
        String actuel = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(decrit)
                .replace("\r\n", "\n") + "\n";

        if ("ecrire".equals(System.getProperty("mappers.instantane"))) {
            Files.createDirectories(INSTANTANE.getParent());
            Files.writeString(INSTANTANE, actuel, StandardCharsets.UTF_8);
        }
        String attendu = Files.readString(INSTANTANE, StandardCharsets.UTF_8).replace("\r\n", "\n");
        assertThat(actuel).isEqualTo(attendu);
    }

    /**
     * Description stable d'un objet : tous ses champs (y compris hérités), triés par nom, avec la
     * classe réelle. Au-delà du premier niveau d'imbrication, une entité est résumée par "Classe#id"
     * pour couper les références circulaires (ligne -> prescription -> lignes...).
     */
    private static Object decrire(Object valeur, int profondeur) {
        if (valeur == null) {
            return null;
        }
        if (valeur instanceof Collection<?> collection) {
            List<Object> elements = new ArrayList<>();
            collection.forEach(element -> elements.add(decrire(element, profondeur)));
            return new TreeMap<>(Map.of("@type", valeur instanceof Set ? "Set" : "List", "elements", elements));
        }
        Class<?> classe = valeur.getClass();
        if (classe.isEnum() || classe.isPrimitive() || valeur instanceof CharSequence || valeur instanceof Number
                || valeur instanceof Boolean || classe.getName().startsWith("java.time.")) {
            return valeur instanceof Enum<?> e ? e.name() : valeur.toString();
        }
        boolean entite = classe.getPackageName().equals(PAQUET_MODELE);
        if (entite && profondeur >= 2) {
            return classe.getSimpleName() + "#" + lire(valeur, "id");
        }
        Map<String, Object> champs = new TreeMap<>();
        champs.put("@type", classe.getSimpleName());
        for (Class<?> c = classe; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field champ : c.getDeclaredFields()) {
                if (Modifier.isStatic(champ.getModifiers())) {
                    continue;
                }
                champ.setAccessible(true);
                try {
                    champs.put(champ.getName(), decrire(champ.get(valeur), entite ? profondeur + 1 : profondeur));
                } catch (IllegalAccessException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
        return champs;
    }

    private static Object lire(Object objet, String nomChamp) {
        for (Class<?> c = objet.getClass(); c != null; c = c.getSuperclass()) {
            try {
                Field champ = c.getDeclaredField(nomChamp);
                champ.setAccessible(true);
                return champ.get(objet);
            } catch (NoSuchFieldException ignore) {
                // champ déclaré dans une classe parente
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return null;
    }
}
