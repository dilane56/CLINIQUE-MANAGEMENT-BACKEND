package org.kfokam48.cliniquemanagementbackend.security;

import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.dto.PageResponse;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;
import org.kfokam48.cliniquemanagementbackend.config.JwtRequestFilter;
import org.kfokam48.cliniquemanagementbackend.config.SecurityConfig;
import org.kfokam48.cliniquemanagementbackend.controller.FactureController;
import org.kfokam48.cliniquemanagementbackend.controller.MedecinController;
import org.kfokam48.cliniquemanagementbackend.controller.PatientController;
import org.kfokam48.cliniquemanagementbackend.controller.RendezVousController;
import org.kfokam48.cliniquemanagementbackend.controller.UtilisateurController;
import org.kfokam48.cliniquemanagementbackend.controller.PrescriptionController;
import org.kfokam48.cliniquemanagementbackend.controller.notification.NotificationRestController;
import org.kfokam48.cliniquemanagementbackend.repository.PrescriptionRepository;
import org.kfokam48.cliniquemanagementbackend.service.PrescriptionService;
import org.kfokam48.cliniquemanagementbackend.service.mail.EmailService;
import org.kfokam48.cliniquemanagementbackend.service.notification.NotificationService;
import org.kfokam48.cliniquemanagementbackend.service.pdf.PdfService;
import org.kfokam48.cliniquemanagementbackend.mapper.MedecinMapper;
import org.kfokam48.cliniquemanagementbackend.mapper.UtilisateurMapper;
import org.kfokam48.cliniquemanagementbackend.service.FactureService;
import org.kfokam48.cliniquemanagementbackend.service.auth.AuthorizationService;
import org.kfokam48.cliniquemanagementbackend.service.auth.CustomUserDetailsService;
import org.kfokam48.cliniquemanagementbackend.service.auth.JwtService;
import org.kfokam48.cliniquemanagementbackend.service.MedecinService;
import org.kfokam48.cliniquemanagementbackend.service.PatientService;
import org.kfokam48.cliniquemanagementbackend.service.RendezVousService;
import org.kfokam48.cliniquemanagementbackend.service.UtilisateurService;
import org.kfokam48.cliniquemanagementbackend.service.TypeRendezVousService;
import org.kfokam48.cliniquemanagementbackend.controller.TypeRendezVousController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie la matrice des droits (points C3 à C8 de AUDIT_BACKEND.md).
 */
@WebMvcTest(controllers = {
        PatientController.class,
        FactureController.class,
        UtilisateurController.class,
        MedecinController.class,
        RendezVousController.class,
        PrescriptionController.class,
        NotificationRestController.class,
        TypeRendezVousController.class,
        org.kfokam48.cliniquemanagementbackend.controller.MessageController.class
}, properties = {
        // Clé de test uniquement (Base64, 256 bits)
        "jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3RzLW9ubHktMzJieXRlcw==",
        "cors.allowed.origins=http://localhost:3000",
        "cors.allowed.methods=GET,POST,PUT,DELETE,OPTIONS,PATCH",
        "cors.allowed.headers=*",
        "cors.allow.credentials=true"
})
@Import({SecurityConfig.class, JwtRequestFilter.class, JwtService.class})
class AccessControlTest {

    private static final long MEDECIN_CONNECTE = 1L;
    private static final long AUTRE_MEDECIN = 2L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean(name = "authz") private AuthorizationService authz;
    @MockitoBean private PatientService patientService;
    @MockitoBean private FactureService factureService;
    @MockitoBean private UtilisateurService utilisateurService;
    @MockitoBean private UtilisateurMapper utilisateurMapper;
    @MockitoBean private MedecinService medecinService;
    @MockitoBean private MedecinMapper medecinMapper;
    @MockitoBean private RendezVousService rendezVousService;
    @MockitoBean private PrescriptionService prescriptionService;
    @MockitoBean private PdfService pdfService;
    @MockitoBean private PrescriptionRepository prescriptionRepository;
    @MockitoBean private NotificationService notificationService;
    @MockitoBean private EmailService emailService;
    @MockitoBean private TypeRendezVousService typeRendezVousService;
    @MockitoBean private org.kfokam48.cliniquemanagementbackend.service.chat.ChatService chatService;

    // --- C3 : plus aucune route métier publique ---

    @Test
    void anonymousCannotListUsers() throws Exception {
        mockMvc.perform(get("/api/utilisateurs")).andExpect(status().isForbidden());
    }

    @Test
    void anonymousCannotListMedecins() throws Exception {
        mockMvc.perform(get("/api/medecins")).andExpect(status().isForbidden());
    }

    // --- C4 : seul l'admin supprime un utilisateur ---

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/utilisateurs/5")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanDeleteUser() throws Exception {
        // deleteById ne renvoie plus rien (I13) : le mock ne fait rien par défaut
        mockMvc.perform(delete("/api/utilisateurs/5")).andExpect(status().isOk());
    }

    // --- Règle métier : un médecin ne supprime pas de patient ---

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotDeletePatient() throws Exception {
        mockMvc.perform(delete("/api/patients/3")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void secretaireCanDeletePatient() throws Exception {
        // deleteById ne renvoie plus rien (I13) : le mock ne fait rien par défaut
        mockMvc.perform(delete("/api/patients/3")).andExpect(status().isOk());
    }

    // --- C6 : factures ---

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void secretaireCanRecordInstallmentPayment() throws Exception {
        mockMvc.perform(put("/api/factures/7/paiement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"montantPaiement\": 5000}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotRecordPayment() throws Exception {
        mockMvc.perform(put("/api/factures/7/paiement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"montantPaiement\": 5000}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void secretaireCannotDeleteFacture() throws Exception {
        mockMvc.perform(delete("/api/factures/7")).andExpect(status().isForbidden());
    }

    // --- C7 : un médecin ne voit que ses propres données ---

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCanSeeOwnRendezVous() throws Exception {
        when(authz.isCurrentUser(MEDECIN_CONNECTE)).thenReturn(true);
        when(rendezVousService.findByMedecinId(anyLong())).thenReturn(List.of());
        mockMvc.perform(get("/api/rendezvous/medecin/" + MEDECIN_CONNECTE)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotSeeOtherMedecinRendezVous() throws Exception {
        when(authz.isCurrentUser(AUTRE_MEDECIN)).thenReturn(false);
        mockMvc.perform(get("/api/rendezvous/medecin/" + AUTRE_MEDECIN)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotSeeOtherMedecinFactures() throws Exception {
        when(authz.isCurrentUser(AUTRE_MEDECIN)).thenReturn(false);
        mockMvc.perform(get("/api/factures/medecin/" + AUTRE_MEDECIN)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void secretaireCanSeeAnyMedecinRendezVous() throws Exception {
        when(rendezVousService.findByMedecinId(anyLong())).thenReturn(List.of());
        mockMvc.perform(get("/api/rendezvous/medecin/" + AUTRE_MEDECIN)).andExpect(status().isOk());
    }

    // --- C8 : faute de frappe SECRETARE corrigée ---

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void secretaireCanViewMedecin() throws Exception {
        mockMvc.perform(get("/api/medecins/1")).andExpect(status().isOk());
    }

    // --- Prescriptions : la secrétaire consulte mais ne modifie pas ---

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void secretaireCanViewPrescription() throws Exception {
        mockMvc.perform(get("/api/prescriptions/4")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void secretaireCannotUpdatePrescription() throws Exception {
        mockMvc.perform(put("/api/prescriptions/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\": \"modif\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void secretaireCannotDeletePrescription() throws Exception {
        mockMvc.perform(delete("/api/prescriptions/4")).andExpect(status().isForbidden());
    }

    // --- Revenus : réservés à l'admin ---
    // (RevenuController non chargé ici : règle hasRole('ADMIN') vérifiée par revue)

    // --- C13 : seul le destinataire marque sa notification comme lue ---

    @Test
    @WithMockUser(roles = "MEDECIN")
    void recipientCanMarkNotificationAsRead() throws Exception {
        when(authz.isNotificationRecipient(9L)).thenReturn(true);
        mockMvc.perform(put("/api/notifications/9/read")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void nonRecipientCannotMarkNotificationAsRead() throws Exception {
        when(authz.isNotificationRecipient(9L)).thenReturn(false);
        mockMvc.perform(put("/api/notifications/9/read")).andExpect(status().isForbidden());
    }

    // --- C7b : contrôle de propriété sur les routes par identifiant ---

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCanViewOwnRendezVous() throws Exception {
        when(authz.ownsRendezVous(10L)).thenReturn(true);
        mockMvc.perform(get("/api/rendezvous/10")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotViewOtherMedecinRendezVous() throws Exception {
        when(authz.ownsRendezVous(11L)).thenReturn(false);
        mockMvc.perform(get("/api/rendezvous/11")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotReassignRendezVousToAnotherMedecin() throws Exception {
        when(authz.ownsRendezVous(10L)).thenReturn(true);
        when(authz.isCurrentUser(AUTRE_MEDECIN)).thenReturn(false);
        mockMvc.perform(put("/api/rendezvous/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dateRendezVous\":\"2026-11-02T10:00:00\",\"patientId\":3,"
                                + "\"medecinId\":" + AUTRE_MEDECIN + ",\"typeRendezVousId\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotChangeStatusOfOtherMedecinRendezVous() throws Exception {
        when(authz.ownsRendezVous(11L)).thenReturn(false);
        mockMvc.perform(patch("/api/rendezvous/11/statut").param("statut", "TERMINE"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotViewOtherMedecinFacture() throws Exception {
        when(authz.ownsFacture(7L)).thenReturn(false);
        mockMvc.perform(get("/api/factures/7")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotViewOtherMedecinPrescription() throws Exception {
        when(authz.ownsPrescription(4L)).thenReturn(false);
        mockMvc.perform(get("/api/prescriptions/4")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCanViewOwnPrescription() throws Exception {
        when(authz.ownsPrescription(4L)).thenReturn(true);
        mockMvc.perform(get("/api/prescriptions/4")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotDeleteOtherMedecinPrescription() throws Exception {
        when(authz.ownsPrescription(4L)).thenReturn(false);
        mockMvc.perform(delete("/api/prescriptions/4")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void secretaireStillViewsAnyRendezVous() throws Exception {
        mockMvc.perform(get("/api/rendezvous/11")).andExpect(status().isOk());
    }

    // --- C14 : envoi d'e-mail libre réservé à l'admin ---

    private static final String MAIL_VALIDE =
            "{\"destinataireEmail\":\"patient@test.com\",\"sujet\":\"Rappel\",\"message\":\"Bonjour\"}";

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotSendFreeEmail() throws Exception {
        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON).content(MAIL_VALIDE))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanSendEmail() throws Exception {
        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON).content(MAIL_VALIDE))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void invalidEmailIsRejected() throws Exception {
        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"destinataireEmail\":\"pas-un-email\",\"sujet\":\"\",\"message\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }

    // --- I11 : pagination progressive ---

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void listWithoutPageParameterIsUnchanged() throws Exception {
        when(patientService.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        verify(patientService, never()).findAll(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void listWithPageParameterIsPaginated() throws Exception {
        when(patientService.findAll(any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(), 1, 5, 12, 3));
        mockMvc.perform(get("/api/patients").param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.totalPages").value(3));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(patientService).findAll(pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void pageSizeIsCappedAt100() throws Exception {
        when(rendezVousService.findAll(any(Pageable.class))).thenReturn(new PageResponse<>(List.of(), 0, 100, 0, 0));
        mockMvc.perform(get("/api/rendezvous").param("page", "0").param("size", "5000"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(rendezVousService).findAll(pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(100);
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void paginatedVariantKeepsTheAccessRule() throws Exception {
        when(authz.isCurrentUser(AUTRE_MEDECIN)).thenReturn(false);
        mockMvc.perform(get("/api/rendezvous/medecin/" + AUTRE_MEDECIN).param("page", "0"))
                .andExpect(status().isForbidden());
    }

    // --- I7 : réponse 401 du filtre JWT en JSON valide ---

    @Test
    void invalidTokenReturnsJson401() throws Exception {
        mockMvc.perform(get("/api/patients").header("Authorization", "Bearer faux.token.jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").isString());
    }

    // --- Alignement frontend : le médecin gère les factures de SES rendez-vous ---

    private static final String FACTURE_RDV_10 =
            "{\"rendezVousId\":10,\"lignesFacture\":[{\"serviceName\":\"Pansement\",\"quantite\":1,\"prixUnitaire\":7000}]}";

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCanCreateInvoiceForOwnAppointment() throws Exception {
        when(authz.ownsRendezVous(10L)).thenReturn(true);
        mockMvc.perform(post("/api/factures").contentType(MediaType.APPLICATION_JSON).content(FACTURE_RDV_10))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotCreateInvoiceForAnotherMedecinAppointment() throws Exception {
        when(authz.ownsRendezVous(10L)).thenReturn(false);
        mockMvc.perform(post("/api/factures").contentType(MediaType.APPLICATION_JSON).content(FACTURE_RDV_10))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCanUpdateAndDeleteOwnInvoiceOnly() throws Exception {
        when(authz.ownsFacture(7L)).thenReturn(true);
        when(authz.ownsRendezVous(10L)).thenReturn(true);
        when(authz.ownsFacture(8L)).thenReturn(false);

        mockMvc.perform(put("/api/factures/7").contentType(MediaType.APPLICATION_JSON).content(FACTURE_RDV_10))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/factures/7")).andExpect(status().isOk());
        mockMvc.perform(put("/api/factures/8").contentType(MediaType.APPLICATION_JSON).content(FACTURE_RDV_10))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/factures/8")).andExpect(status().isForbidden());
    }

    // --- Alignement frontend : la secrétaire gère les types de rendez-vous ---

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void secretaireCanManageAppointmentTypes() throws Exception {
        String type = "{\"libelle\":\"Vaccination\",\"duree\":15,\"tarif\":10000}";
        mockMvc.perform(post("/api/type-rendezvous").contentType(MediaType.APPLICATION_JSON).content(type))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/type-rendezvous/3").contentType(MediaType.APPLICATION_JSON).content(type))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/type-rendezvous/3")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotManageAppointmentTypes() throws Exception {
        mockMvc.perform(delete("/api/type-rendezvous/3")).andExpect(status().isForbidden());
    }

    // --- Alignement frontend : modification de son propre profil ---

    @Test
    @WithMockUser(username = "medecin@clinique.com", roles = "MEDECIN")
    void anyoneCanUpdateOwnProfile() throws Exception {
        mockMvc.perform(put("/api/utilisateurs/me").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"Martin\",\"prenom\":\"Paul\",\"telephone\":\"690000000\",\"adresse\":\"Douala\"}"))
                .andExpect(status().isOk());
        // L'utilisateur modifié est celui du JWT, jamais un identifiant fourni par le client
        verify(utilisateurService).updateProfil(org.mockito.ArgumentMatchers.eq("medecin@clinique.com"), any());
    }

    @Test
    void anonymousCannotUpdateProfile() throws Exception {
        mockMvc.perform(put("/api/utilisateurs/me").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"X\",\"prenom\":\"Y\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void invalidProfileIsRejected() throws Exception {
        mockMvc.perform(put("/api/utilisateurs/me").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"\",\"prenom\":\"Y\",\"telephone\":\"12\"}"))
                .andExpect(status().isBadRequest());
    }

    // --- Alignement frontend : modification de prescription sans rendezVousId ---

    @Test
    @WithMockUser(roles = "MEDECIN")
    void prescriptionUpdateWithoutAppointmentKeepsTheOwnershipCheckOnThePrescription() throws Exception {
        when(authz.ownsPrescription(4L)).thenReturn(true);
        mockMvc.perform(put("/api/prescriptions/4").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"modif\"}"))
                .andExpect(status().isOk());
        when(authz.ownsPrescription(5L)).thenReturn(false);
        mockMvc.perform(put("/api/prescriptions/5").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"modif\"}"))
                .andExpect(status().isForbidden());
    }

    // --- Recherche paginée des rendez-vous (page Rendez-vous de l'admin) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void appointmentSearchPassesCriteriaAndDefaultsToMostRecentFirst() throws Exception {
        when(rendezVousService.rechercher(any(), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0));

        mockMvc.perform(get("/api/rendezvous/recherche")
                        .param("q", "martin").param("statut", "CONFIRME").param("date", "2030-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(rendezVousService).rechercher(org.mockito.ArgumentMatchers.eq(
                org.kfokam48.cliniquemanagementbackend.dto.rendezvous.CriteresRendezVous.de("martin",
                        org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous.CONFIRME, java.time.LocalDate.of(2030, 1, 15))),
                pageable.capture());
        assertThat(pageable.getValue().getSort().getOrderFor("dateRendezVous")).isNotNull();
        assertThat(pageable.getValue().getSort().getOrderFor("dateRendezVous").isDescending()).isTrue();
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void doctorSearchesOnlyOwnAppointments() throws Exception {
        when(rendezVousService.rechercher(any(), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0));
        when(authz.isCurrentUser(MEDECIN_CONNECTE)).thenReturn(true);
        when(authz.isCurrentUser(AUTRE_MEDECIN)).thenReturn(false);

        mockMvc.perform(get("/api/rendezvous/recherche").param("medecinId", String.valueOf(MEDECIN_CONNECTE))
                        .param("debut", "2030-01-13").param("fin", "2030-01-19")
                        .param("statut", "EN_COURS").param("statut", "TERMINE").param("sansFacture", "true"))
                .andExpect(status().isOk());
        verify(rendezVousService).rechercher(org.mockito.ArgumentMatchers.eq(
                new org.kfokam48.cliniquemanagementbackend.dto.rendezvous.CriteresRendezVous(null,
                        List.of(org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous.EN_COURS,
                                org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous.TERMINE),
                        null, java.time.LocalDate.of(2030, 1, 13), java.time.LocalDate.of(2030, 1, 19),
                        MEDECIN_CONNECTE, true)), any(Pageable.class));

        // Ni les rendez-vous d'un autre médecin, ni la recherche sans médecin
        mockMvc.perform(get("/api/rendezvous/recherche").param("medecinId", String.valueOf(AUTRE_MEDECIN)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/rendezvous/recherche")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void invalidStatusIsRejected() throws Exception {
        mockMvc.perform(get("/api/rendezvous/recherche").param("statut", "INCONNU"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void secretaireCanSearchAndCountAppointments() throws Exception {
        mockMvc.perform(get("/api/rendezvous/recherche")).andExpect(status().isOk());
        mockMvc.perform(get("/api/rendezvous/statistiques")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotSearchAllAppointments() throws Exception {
        mockMvc.perform(get("/api/rendezvous/recherche")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/rendezvous/statistiques")).andExpect(status().isForbidden());
    }

    // --- I5 : désactivation d'un compte par l'administrateur ---

    @Test
    @WithMockUser(username = "admin@clinique.com", roles = "ADMIN")
    void adminCanDisableAnAccount() throws Exception {
        mockMvc.perform(patch("/api/utilisateurs/5/activation").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actif\":false}"))
                .andExpect(status().isOk());
        // L'administrateur qui agit est celui du JWT (pour lui interdire de se désactiver lui-même)
        verify(utilisateurService).changerActivation(5L, false, "admin@clinique.com");
    }

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void onlyAdminCanDisableAnAccount() throws Exception {
        mockMvc.perform(patch("/api/utilisateurs/5/activation").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actif\":false}"))
                .andExpect(status().isForbidden());
        verify(utilisateurService, never()).changerActivation(anyLong(), org.mockito.ArgumentMatchers.anyBoolean(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activationWithoutValueIsRejected() throws Exception {
        mockMvc.perform(patch("/api/utilisateurs/5/activation").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    private static String jetonPour(String email) {
        return io.jsonwebtoken.Jwts.builder()
                .subject(email)
                .expiration(new java.util.Date(System.currentTimeMillis() + 60_000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64
                        .decode("dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3RzLW9ubHktMzJieXRlcw==")))
                .compact();
    }

    private void compteAdmin(String email, boolean actif) {
        when(customUserDetailsService.loadUserByUsername(email)).thenReturn(new org.kfokam48.cliniquemanagementbackend.model.auth.CustomUserDetails(
                email, "hash", java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")), actif));
    }

    @Test
    void validTokenOfActiveAccountIsAccepted() throws Exception {
        compteAdmin("actif@clinique.com", true);
        mockMvc.perform(get("/api/utilisateurs").header("Authorization", "Bearer " + jetonPour("actif@clinique.com")))
                .andExpect(status().isOk());
    }

    @Test
    void validTokenOfDisabledAccountIsRejectedImmediately() throws Exception {
        compteAdmin("desactive@clinique.com", false);
        mockMvc.perform(get("/api/utilisateurs").header("Authorization", "Bearer " + jetonPour("desactive@clinique.com")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Compte désactivé. Contactez l'administrateur."));
        verify(utilisateurService, never()).findAll();
    }

    // --- Compteurs des tableaux de bord ---

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void secretaireCanReadInvoiceCounts() throws Exception {
        when(factureService.compterParStatut()).thenReturn(java.util.Map.of(
                org.kfokam48.cliniquemanagementbackend.enums.StatutFacture.NON_PAYEE, 3L));
        mockMvc.perform(get("/api/factures/statistiques"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.NON_PAYEE").value(3));
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotReadInvoiceCounts() throws Exception {
        mockMvc.perform(get("/api/factures/statistiques")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanReadAccountCounts() throws Exception {
        when(utilisateurService.compterParRole()).thenReturn(java.util.Map.of(org.kfokam48.cliniquemanagementbackend.enums.Roles.MEDECIN, 5L));
        mockMvc.perform(get("/api/utilisateurs/statistiques"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.MEDECIN").value(5));
    }

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void onlyAdminCanReadAccountCounts() throws Exception {
        mockMvc.perform(get("/api/utilisateurs/statistiques")).andExpect(status().isForbidden());
    }

    // --- Historique paginé du chat (P7) ---

    @Test
    @WithMockUser(roles = "MEDECIN")
    void chatHistoryIsPagedMostRecentFirstAndOnlyForParticipants() throws Exception {
        when(authz.isCurrentUser(MEDECIN_CONNECTE)).thenReturn(true);
        when(authz.isCurrentUser(AUTRE_MEDECIN)).thenReturn(false);
        when(authz.isCurrentUser(3L)).thenReturn(false);
        when(chatService.getConversation(any(), any(), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(), 0, 30, 0, 0));

        mockMvc.perform(get("/api/messages/conversation/" + MEDECIN_CONNECTE + "/" + AUTRE_MEDECIN).param("page", "0"))
                .andExpect(status().isOk());
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(chatService).getConversation(org.mockito.ArgumentMatchers.eq(MEDECIN_CONNECTE),
                org.mockito.ArgumentMatchers.eq(AUTRE_MEDECIN), pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(30);
        assertThat(pageable.getValue().getSort().getOrderFor("dateEnvoi").isDescending()).isTrue();

        // Conversation entre deux autres utilisateurs : refusée
        mockMvc.perform(get("/api/messages/conversation/" + AUTRE_MEDECIN + "/3").param("page", "0"))
                .andExpect(status().isForbidden());
    }

    // --- Recherche de patients (choix du patient d'un rendez-vous) ---

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void patientSearchPassesTextAndDefaultsToTenByName() throws Exception {
        when(patientService.rechercher(any(), any(), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(), 0, 10, 0, 0));

        mockMvc.perform(get("/api/patients/recherche").param("q", "fotso"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(patientService).rechercher(org.mockito.ArgumentMatchers.eq("fotso"), org.mockito.ArgumentMatchers.isNull(), pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(10);
        assertThat(pageable.getValue().getSort().getOrderFor("nom")).isNotNull();
    }

    @Test
    void anonymousCannotSearchPatients() throws Exception {
        mockMvc.perform(get("/api/patients/recherche").param("q", "fotso")).andExpect(status().isForbidden());
    }
}
