package org.kfokam48.cliniquemanagementbackend.service;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.enums.Sexe;
import org.kfokam48.cliniquemanagementbackend.enums.StatutFacture;
import org.kfokam48.cliniquemanagementbackend.model.*;
import org.kfokam48.cliniquemanagementbackend.service.pdf.PdfService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * I15 : génération des PDF avec OpenPDF (remplaçant d'iText 5). Le rendu visuel reste à vérifier
 * à l'œil ; ce test garantit que les deux documents se génèrent et contiennent les informations.
 */
class PdfServiceTest {

    private final PdfService pdfService = new PdfService();

    private static RendezVous rendezVous() {
        Medecin medecin = new Medecin();
        medecin.setId(7L);
        medecin.setNom("Martin");
        medecin.setPrenom("Paul");
        medecin.setSpecialite("Généraliste");
        medecin.setEmail("martin@clinique.com");

        Patient patient = new Patient();
        patient.setNom("Ngono");
        patient.setPrenom("Aïcha");
        patient.setDateNaissance(LocalDate.of(1990, 5, 12));
        patient.setSexe(Sexe.FEMME);
        patient.setAllergies("Pénicilline");

        RendezVous rendezVous = new RendezVous();
        rendezVous.setMedecin(medecin);
        rendezVous.setPatient(patient);
        return rendezVous;
    }

    private static String texte(byte[] pdf) throws Exception {
        PdfReader reader = new PdfReader(pdf);
        try {
            StringBuilder texte = new StringBuilder();
            for (int page = 1; page <= reader.getNumberOfPages(); page++) {
                texte.append(new PdfTextExtractor(reader).getTextFromPage(page));
            }
            return texte.toString();
        } finally {
            reader.close();
        }
    }

    @Test
    void invoicePdfIsGenerated() throws Exception {
        LigneFacture ligne = new LigneFacture();
        ligne.setServiceName("Consultation générale");
        ligne.setQuantite(1);
        ligne.setPrixUnitaire(new BigDecimal("15000.00"));
        ligne.setPrixTotal(new BigDecimal("15000.00"));

        Facture facture = new Facture();
        facture.setId(42L);
        facture.setRendezVous(rendezVous());
        facture.setDateEmission(LocalDateTime.of(2030, 1, 15, 10, 30));
        facture.setStatut(StatutFacture.PARTIELLEMENT_PAYE);
        facture.setMontantTotal(new BigDecimal("15000.00"));
        facture.setMontantPayement(new BigDecimal("5000.00"));
        facture.setMontantRestant(new BigDecimal("10000.00"));
        facture.setLignes(List.of(ligne));

        byte[] pdf = pdfService.generateFacturePdf(facture).toByteArray();

        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
        assertThat(texte(pdf))
                .contains("FACTURE MÉDICALE", "FAC-000042", "Ngono", "Consultation générale", "Martin");
    }

    @Test
    void prescriptionPdfIsGenerated() throws Exception {
        LignePrescription ligne = new LignePrescription();
        ligne.setMedicament("Paracétamol");
        ligne.setDosage("500 mg");
        ligne.setFrequence("3 fois par jour");
        ligne.setDuree(5);

        Prescription prescription = new Prescription();
        prescription.setRendezVous(rendezVous());
        prescription.setDate(LocalDate.of(2030, 1, 15));
        prescription.setDescription("Fièvre");
        prescription.setLignes(List.of(ligne));

        byte[] pdf = pdfService.generatePrescriptionPdf(prescription).toByteArray();

        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
        assertThat(texte(pdf))
                .contains("PRESCRIPTION MÉDICALE", "Paracétamol", "Pénicilline", "Aïcha", "Martin");
    }
}
