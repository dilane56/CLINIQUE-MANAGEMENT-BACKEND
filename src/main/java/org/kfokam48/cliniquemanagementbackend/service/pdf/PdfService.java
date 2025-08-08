package org.kfokam48.cliniquemanagementbackend.service.pdf;


import com.itextpdf.text.*;

import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import org.kfokam48.cliniquemanagementbackend.model.*;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;


@Service
public class PdfService {
    private final Font title_Font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 30, BaseColor.BLUE);
    private final Font subTitle_Font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLUE);
    private final Font text_Font = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
    private final Font text_Font2 = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);


    // Ajustement du modèle de PDF pour la facture
    public ByteArrayOutputStream generateFacturePdf(Facture facture) throws DocumentException {
        Document document = new Document(PageSize.A4, 50, 50, 80, 50);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        // En-tête avec informations de la clinique
        addFactureHeader(document);
        
        // Titre principal
        Paragraph title = new Paragraph("FACTURE MÉDICALE", title_Font);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Informations de la facture
        addFactureInfo(document, facture);
        
        // Informations du patient
        if (facture.getRendezVous() != null && facture.getRendezVous().getPatient() != null) {
            addPatientInfoToFacture(document, facture.getRendezVous().getPatient());
        }
        
        // Informations du médecin
        if (facture.getRendezVous() != null && facture.getRendezVous().getMedecin() != null) {
            addDoctorInfoToFacture(document, facture.getRendezVous().getMedecin());
        }
        
        // Tableau des services
        addServicesTable(document, facture.getLignes());
        
        // Calculs financiers
        addFinancialSummary(document, facture);
        
        // Conditions de paiement
        addPaymentTerms(document);
        
        // Signature et cachet
        addFactureSignatureSection(document, facture);
        
        // Pied de page
        addFactureFooter(document);

        document.close();
        return outputStream;
    }

    private void addFactureHeader(Document document) throws DocumentException {
        // En-tête avec informations de la clinique
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1f, 1f});
        
        // Logo/Informations clinique (côté gauche)
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setVerticalAlignment(Element.ALIGN_TOP);
        
        Paragraph clinicInfo = new Paragraph();
        clinicInfo.add(new Chunk("CLINIQUE MÉDICALE\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY)));
        clinicInfo.add(new Chunk("123 Avenue de la Santé\n", text_Font));
        clinicInfo.add(new Chunk("Douala, Cameroun\n", text_Font));
        clinicInfo.add(new Chunk("Tél: +237 XXX XXX XXX\n", text_Font));
        clinicInfo.add(new Chunk("Email: contact@clinique.com\n", text_Font));
        clinicInfo.add(new Chunk("Site: www.clinique.com\n", text_Font));
        leftCell.addElement(clinicInfo);
        
        // Informations de facturation (côté droit)
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setVerticalAlignment(Element.ALIGN_TOP);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        Paragraph billingInfo = new Paragraph();
        billingInfo.add(new Chunk("FACTURE\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLUE)));
        billingInfo.add(new Chunk("Date: " + java.time.LocalDate.now().toString() + "\n", text_Font));
        billingInfo.add(new Chunk("Échéance: " + java.time.LocalDate.now().plusDays(30).toString() + "\n", text_Font));
        billingInfo.add(new Chunk("Mode de paiement: Espèces/Carte\n", text_Font));
        rightCell.addElement(billingInfo);
        
        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);
        document.add(headerTable);
        
        // Ligne de séparation
        LineSeparator separator = new LineSeparator();
        separator.setLineWidth(1);
        separator.setLineColor(BaseColor.GRAY);
        document.add(separator);
        document.add(new Paragraph(" ")); // Espace
    }

    private void addFactureInfo(Document document, Facture facture) throws DocumentException {
        PdfPTable factureInfoTable = new PdfPTable(2);
        factureInfoTable.setWidthPercentage(100);
        factureInfoTable.setWidths(new float[]{0.3f, 0.7f});
        
        factureInfoTable.addCell(createLabelCell("Numéro de Facture:"));
        factureInfoTable.addCell(createValueCell("FAC-" + String.format("%06d", facture.getId())));
        
        factureInfoTable.addCell(createLabelCell("Date d'Émission:"));
        factureInfoTable.addCell(createValueCell(facture.getDateEmission().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        
        if (facture.getDatePayement() != null) {
            factureInfoTable.addCell(createLabelCell("Date de Paiement:"));
            factureInfoTable.addCell(createValueCell(facture.getDatePayement().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        }
        
        factureInfoTable.addCell(createLabelCell("Statut:"));
        PdfPCell statusCell = createValueCell(facture.getStatut().toString());
        if (facture.getStatut().toString().equals("PAYEE")) {
            statusCell.setBackgroundColor(BaseColor.GREEN);
        } else if (facture.getStatut().toString().equals("NON_PAYEE")) {
            statusCell.setBackgroundColor(BaseColor.RED);
        } else {
            statusCell.setBackgroundColor(BaseColor.YELLOW);
        }
        factureInfoTable.addCell(statusCell);
        
        document.add(factureInfoTable);
        document.add(new Paragraph(" ")); // Espace
    }

    private void addPatientInfoToFacture(Document document, Patient patient) throws DocumentException {
        Paragraph patientTitle = new Paragraph("INFORMATIONS DU PATIENT", subTitle_Font);
        patientTitle.setSpacingBefore(10);
        patientTitle.setSpacingAfter(5);
        document.add(patientTitle);
        
        PdfPTable patientTable = new PdfPTable(2);
        patientTable.setWidthPercentage(100);
        patientTable.setWidths(new float[]{0.3f, 0.7f});
        
        patientTable.addCell(createLabelCell("Nom et Prénom:"));
        patientTable.addCell(createValueCell(patient.getNom() + " " + patient.getPrenom()));
        
        patientTable.addCell(createLabelCell("Date de Naissance:"));
        patientTable.addCell(createValueCell(patient.getDateNaissance().toString()));
        
        patientTable.addCell(createLabelCell("Sexe:"));
        patientTable.addCell(createValueCell(patient.getSexe().toString()));
        
        patientTable.addCell(createLabelCell("Téléphone:"));
        patientTable.addCell(createValueCell(patient.getTelephone()));
        
        patientTable.addCell(createLabelCell("Adresse:"));
        patientTable.addCell(createValueCell(patient.getAdresse()));
        
        document.add(patientTable);
        document.add(new Paragraph(" ")); // Espace
    }

    private void addDoctorInfoToFacture(Document document, Medecin medecin) throws DocumentException {
        Paragraph doctorTitle = new Paragraph("MÉDECIN CONSULTANT", subTitle_Font);
        doctorTitle.setSpacingBefore(10);
        doctorTitle.setSpacingAfter(5);
        document.add(doctorTitle);
        
        PdfPTable doctorTable = new PdfPTable(2);
        doctorTable.setWidthPercentage(100);
        doctorTable.setWidths(new float[]{0.3f, 0.7f});
        
        doctorTable.addCell(createLabelCell("Nom et Prénom:"));
        doctorTable.addCell(createValueCell("Dr. " + medecin.getNom() + " " + medecin.getPrenom()));
        
        doctorTable.addCell(createLabelCell("Spécialité:"));
        doctorTable.addCell(createValueCell(medecin.getSpecialite()));
        
        doctorTable.addCell(createLabelCell("Téléphone:"));
        doctorTable.addCell(createValueCell(medecin.getTelephone()));
        
        doctorTable.addCell(createLabelCell("Email:"));
        doctorTable.addCell(createValueCell(medecin.getEmail()));
        
        document.add(doctorTable);
        document.add(new Paragraph(" ")); // Espace
    }

    private void addServicesTable(Document document, List<LigneFacture> lignes) throws DocumentException {
        Paragraph servicesTitle = new Paragraph("DÉTAIL DES SERVICES", subTitle_Font);
        servicesTitle.setSpacingBefore(10);
        servicesTitle.setSpacingAfter(10);
        document.add(servicesTitle);
        
        PdfPTable servicesTable = new PdfPTable(5);
        servicesTable.setWidthPercentage(100);
        servicesTable.setWidths(new float[]{0.4f, 0.15f, 0.15f, 0.15f, 0.15f});
        
        // En-têtes du tableau
        String[] headers = {"Description", "Quantité", "Prix Unitaire", "TVA (19%)", "Total HT"};
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE)));
            headerCell.setBackgroundColor(BaseColor.DARK_GRAY);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerCell.setPadding(5);
            servicesTable.addCell(headerCell);
        }
        
        // Données des services
        for (LigneFacture ligne : lignes) {
            servicesTable.addCell(createServiceCell(ligne.getServiceName()));
            servicesTable.addCell(createServiceCell(String.valueOf(ligne.getQuantite())));
            servicesTable.addCell(createServiceCell(ligne.getPrixUnitaire().toString() + " FCFA"));
            servicesTable.addCell(createServiceCell("19%"));
            servicesTable.addCell(createServiceCell(ligne.getPrixTotal().toString() + " FCFA"));
        }
        
        document.add(servicesTable);
        document.add(new Paragraph(" ")); // Espace
    }

    private void addFinancialSummary(Document document, Facture facture) throws DocumentException {
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(50);
        summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        summaryTable.setWidths(new float[]{0.6f, 0.4f});
        
        // Calculs
        BigDecimal totalHT = facture.getMontantTotal();
        BigDecimal tva = totalHT.multiply(new BigDecimal("0.19"));
        BigDecimal totalTTC = totalHT.add(tva);
        
        summaryTable.addCell(createSummaryLabelCell("Total HT:"));
        summaryTable.addCell(createSummaryValueCell(totalHT.toString() + " FCFA"));
        
        summaryTable.addCell(createSummaryLabelCell("TVA (19%):"));
        summaryTable.addCell(createSummaryValueCell(tva.toString() + " FCFA"));
        
        summaryTable.addCell(createSummaryLabelCell("Total TTC:"));
        summaryTable.addCell(createSummaryValueCell(totalTTC.toString() + " FCFA"));
        
        summaryTable.addCell(createSummaryLabelCell("Montant Payé:"));
        summaryTable.addCell(createSummaryValueCell(facture.getMontantPayement().toString() + " FCFA"));
        
        summaryTable.addCell(createSummaryLabelCell("Montant Restant:"));
        PdfPCell restantCell = createSummaryValueCell(facture.getMontantRestant().toString() + " FCFA");
        if (facture.getMontantRestant().compareTo(BigDecimal.ZERO) > 0) {
            restantCell.setBackgroundColor(BaseColor.RED);
        }
        summaryTable.addCell(restantCell);
        
        document.add(summaryTable);
        document.add(new Paragraph(" ")); // Espace
    }

    private void addPaymentTerms(Document document) throws DocumentException {
        Paragraph termsTitle = new Paragraph("CONDITIONS DE PAIEMENT", subTitle_Font);
        termsTitle.setSpacingBefore(10);
        termsTitle.setSpacingAfter(5);
        document.add(termsTitle);
        
        PdfPTable termsTable = new PdfPTable(1);
        termsTable.setWidthPercentage(100);
        
        String[] terms = {
            "• Paiement à réception de facture",
            "• Délai de paiement: 30 jours",
            "• Modes de paiement acceptés: Espèces, Carte bancaire, Chèque",
            "• En cas de retard de paiement, des pénalités de 5% par mois seront appliquées",
            "• Pour toute question concernant cette facture, contactez notre service comptabilité"
        };
        
        for (String term : terms) {
            PdfPCell cell = new PdfPCell(new Phrase(term, text_Font));
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPadding(2);
            termsTable.addCell(cell);
        }
        
        document.add(termsTable);
        document.add(new Paragraph(" ")); // Espace
    }

    private void addFactureSignatureSection(Document document, Facture facture) throws DocumentException {
        PdfPTable signatureTable = new PdfPTable(2);
        signatureTable.setWidthPercentage(100);
        signatureTable.setWidths(new float[]{0.5f, 0.5f});
        
        // Signature du patient
        PdfPCell patientSignatureCell = new PdfPCell();
        patientSignatureCell.setBorder(Rectangle.NO_BORDER);
        patientSignatureCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        Paragraph patientSignature = new Paragraph();
        patientSignature.add(new Chunk("Signature du Patient\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK)));
        patientSignature.add(new Chunk("_________________________\n", text_Font));
        if (facture.getRendezVous() != null && facture.getRendezVous().getPatient() != null) {
            patientSignature.add(new Chunk(facture.getRendezVous().getPatient().getNom() + " " + facture.getRendezVous().getPatient().getPrenom() + "\n", text_Font));
        }
        patientSignature.add(new Chunk("Date: _________________\n", text_Font));
        
        patientSignatureCell.addElement(patientSignature);
        
        // Cachet de la clinique
        PdfPCell clinicStampCell = new PdfPCell();
        clinicStampCell.setBorder(Rectangle.NO_BORDER);
        clinicStampCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        Paragraph clinicStamp = new Paragraph();
        clinicStamp.add(new Chunk("Cachet de la Clinique\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK)));
        clinicStamp.add(new Chunk("_________________________\n", text_Font));
        clinicStamp.add(new Chunk("CLINIQUE MÉDICALE\n", text_Font));
        clinicStamp.add(new Chunk("Date: " + java.time.LocalDate.now().toString(), text_Font));
        
        clinicStampCell.addElement(clinicStamp);
        
        signatureTable.addCell(patientSignatureCell);
        signatureTable.addCell(clinicStampCell);
        
        document.add(signatureTable);
    }

    private void addFactureFooter(Document document) throws DocumentException {
        document.add(new Paragraph(" ")); // Espace
        
        LineSeparator footerSeparator = new LineSeparator();
        footerSeparator.setLineWidth(0.5f);
        footerSeparator.setLineColor(BaseColor.GRAY);
        document.add(footerSeparator);
        
        Paragraph footer = new Paragraph();
        footer.add(new Chunk("Merci de votre confiance. Pour toute question, contactez notre service comptabilité au +237 XXX XXX XXX.\n", 
                FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY)));
        footer.add(new Chunk("Cette facture est générée automatiquement par le système de gestion de la clinique.", 
                FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY)));
        footer.setAlignment(Element.ALIGN_CENTER);
        
        document.add(footer);
    }

    // Ajustement du modèle de PDF pour la prescription
    public ByteArrayOutputStream generatePrescriptionPdf(Prescription prescription) throws DocumentException {
        Document document = new Document(PageSize.A4, 50, 50, 80, 50);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        // En-tête avec logo et informations de la clinique
        addHeader(document);
        
        // Titre principal
        Paragraph title = new Paragraph("PRESCRIPTION MÉDICALE", title_Font);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Informations du médecin prescripteur
        addDoctorInfo(document, prescription.getRendezVous().getMedecin());
        
        // Informations du patient
        addPatientInfo(document, prescription.getRendezVous().getPatient());
        
        // Date de prescription
        addPrescriptionDate(document, prescription.getDate());
        
        // Description/Diagnostic
        if (prescription.getDescription() != null && !prescription.getDescription().trim().isEmpty()) {
            addDiagnosis(document, prescription.getDescription());
        }
        
        // Tableau des médicaments amélioré
        addMedicationTable(document, prescription.getLignes());
        
        // Instructions générales
        addGeneralInstructions(document);
        
        // Signature et cachet
        addSignatureSection(document, prescription.getRendezVous().getMedecin());
        
        // Pied de page
        addFooter(document);

        document.close();
        return outputStream;
    }

    private void addHeader(Document document) throws DocumentException {
        // En-tête avec informations de la clinique
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1f, 1f});
        
        // Logo/Informations clinique (côté gauche)
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setVerticalAlignment(Element.ALIGN_TOP);
        
        Paragraph clinicInfo = new Paragraph();
        clinicInfo.add(new Chunk("CLINIQUE MÉDICALE\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.DARK_GRAY)));
        clinicInfo.add(new Chunk("123 Avenue de la Santé\n", text_Font));
        clinicInfo.add(new Chunk("Tél: +237 XXX XXX XXX\n", text_Font));
        clinicInfo.add(new Chunk("Email: contact@clinique.com\n", text_Font));
        leftCell.addElement(clinicInfo);
        
        // Informations de contact (côté droit)
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setVerticalAlignment(Element.ALIGN_TOP);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        Paragraph contactInfo = new Paragraph();
        contactInfo.add(new Chunk("Urgences: +237 XXX XXX XXX\n", text_Font));
        contactInfo.add(new Chunk("Site web: www.clinique.com\n", text_Font));
        rightCell.addElement(contactInfo);
        
        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);
        document.add(headerTable);
        
        // Ligne de séparation
        LineSeparator separator = new LineSeparator();
        separator.setLineWidth(1);
        separator.setLineColor(BaseColor.GRAY);
        document.add(separator);
        document.add(new Paragraph(" ")); // Espace
    }

    private void addDoctorInfo(Document document, Medecin medecin) throws DocumentException {
        Paragraph doctorTitle = new Paragraph("MÉDECIN PRESCRIPTEUR", subTitle_Font);
        doctorTitle.setSpacingBefore(10);
        doctorTitle.setSpacingAfter(5);
        document.add(doctorTitle);
        
        PdfPTable doctorTable = new PdfPTable(2);
        doctorTable.setWidthPercentage(100);
        doctorTable.setWidths(new float[]{0.3f, 0.7f});
        
        doctorTable.addCell(createLabelCell("Nom et Prénom:"));
        doctorTable.addCell(createValueCell("Dr. " + medecin.getNom() + " " + medecin.getPrenom()));
        
        doctorTable.addCell(createLabelCell("Spécialité:"));
        doctorTable.addCell(createValueCell(medecin.getSpecialite()));
        
        doctorTable.addCell(createLabelCell("Téléphone:"));
        doctorTable.addCell(createValueCell(medecin.getTelephone()));
        
        doctorTable.addCell(createLabelCell("Email:"));
        doctorTable.addCell(createValueCell(medecin.getEmail()));
        
        document.add(doctorTable);
        document.add(new Paragraph(" ")); // Espace
    }

    private void addPatientInfo(Document document, Patient patient) throws DocumentException {
        Paragraph patientTitle = new Paragraph("INFORMATIONS DU PATIENT", subTitle_Font);
        patientTitle.setSpacingBefore(10);
        patientTitle.setSpacingAfter(5);
        document.add(patientTitle);
        
        PdfPTable patientTable = new PdfPTable(2);
        patientTable.setWidthPercentage(100);
        patientTable.setWidths(new float[]{0.3f, 0.7f});
        
        patientTable.addCell(createLabelCell("Nom et Prénom:"));
        patientTable.addCell(createValueCell(patient.getNom() + " " + patient.getPrenom()));
        
        patientTable.addCell(createLabelCell("Date de Naissance:"));
        patientTable.addCell(createValueCell(patient.getDateNaissance().toString()));
        
        patientTable.addCell(createLabelCell("Sexe:"));
        patientTable.addCell(createValueCell(patient.getSexe().toString()));
        
        patientTable.addCell(createLabelCell("Téléphone:"));
        patientTable.addCell(createValueCell(patient.getTelephone()));
        
        patientTable.addCell(createLabelCell("Adresse:"));
        patientTable.addCell(createValueCell(patient.getAdresse()));
        
        document.add(patientTable);
        
        // Antécédents médicaux
        if (patient.getAntecedents() != null && !patient.getAntecedents().trim().isEmpty()) {
            document.add(new Paragraph(" "));
            PdfPTable antecedentsTable = new PdfPTable(2);
            antecedentsTable.setWidthPercentage(100);
            antecedentsTable.setWidths(new float[]{0.3f, 0.7f});
            
            antecedentsTable.addCell(createLabelCell("Antécédents:"));
            antecedentsTable.addCell(createValueCell(patient.getAntecedents()));
            
            document.add(antecedentsTable);
        }
        
        // Allergies
        if (patient.getAllergies() != null && !patient.getAllergies().trim().isEmpty()) {
            document.add(new Paragraph(" "));
            PdfPTable allergiesTable = new PdfPTable(2);
            allergiesTable.setWidthPercentage(100);
            allergiesTable.setWidths(new float[]{0.3f, 0.7f});
            
            PdfPCell allergyLabel = new PdfPCell(new Phrase("Allergies:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE)));
            allergyLabel.setBackgroundColor(BaseColor.RED);
            allergyLabel.setPadding(5);
            allergyLabel.setVerticalAlignment(Element.ALIGN_MIDDLE);
            allergiesTable.addCell(allergyLabel);
            allergiesTable.addCell(createValueCell(patient.getAllergies()));
            
            document.add(allergiesTable);
        }
        
        document.add(new Paragraph(" ")); // Espace
    }

    private void addPrescriptionDate(Document document, LocalDate date) throws DocumentException {
        Paragraph dateTitle = new Paragraph("DATE DE PRESCRIPTION", subTitle_Font);
        dateTitle.setSpacingBefore(10);
        dateTitle.setSpacingAfter(5);
        document.add(dateTitle);
        
        PdfPTable dateTable = new PdfPTable(2);
        dateTable.setWidthPercentage(100);
        dateTable.setWidths(new float[]{0.3f, 0.7f});
        
        dateTable.addCell(createLabelCell("Date:"));
        dateTable.addCell(createValueCell(date.toString()));
        
        document.add(dateTable);
        document.add(new Paragraph(" ")); // Espace
    }

    private void addDiagnosis(Document document, String description) throws DocumentException {
        Paragraph diagnosisTitle = new Paragraph("DIAGNOSTIC / MOTIF", subTitle_Font);
        diagnosisTitle.setSpacingBefore(10);
        diagnosisTitle.setSpacingAfter(5);
        document.add(diagnosisTitle);
        
        PdfPTable diagnosisTable = new PdfPTable(2);
        diagnosisTable.setWidthPercentage(100);
        diagnosisTable.setWidths(new float[]{0.3f, 0.7f});
        
        diagnosisTable.addCell(createLabelCell("Diagnostic:"));
        diagnosisTable.addCell(createValueCell(description));
        
        document.add(diagnosisTable);
        document.add(new Paragraph(" ")); // Espace
    }

    private void addMedicationTable(Document document, List<LignePrescription> lignes) throws DocumentException {
        Paragraph medicationTitle = new Paragraph("PRESCRIPTION MÉDICAMENTEUSE", subTitle_Font);
        medicationTitle.setSpacingBefore(10);
        medicationTitle.setSpacingAfter(10);
        document.add(medicationTitle);
        
        PdfPTable medicationTable = new PdfPTable(5);
        medicationTable.setWidthPercentage(100);
        medicationTable.setWidths(new float[]{0.25f, 0.2f, 0.2f, 0.15f, 0.2f});
        
        // En-têtes du tableau
        String[] headers = {"Médicament", "Dosage", "Fréquence", "Durée", "Instructions"};
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE)));
            headerCell.setBackgroundColor(BaseColor.DARK_GRAY);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerCell.setPadding(5);
            medicationTable.addCell(headerCell);
        }
        
        // Données des médicaments
        for (LignePrescription ligne : lignes) {
            medicationTable.addCell(createMedicationCell(ligne.getMedicament()));
            medicationTable.addCell(createMedicationCell(ligne.getDosage()));
            medicationTable.addCell(createMedicationCell(ligne.getFrequence()));
            medicationTable.addCell(createMedicationCell(ligne.getDuree() + " jours"));
            medicationTable.addCell(createMedicationCell("À prendre avec de l'eau"));
        }
        
        document.add(medicationTable);
        document.add(new Paragraph(" ")); // Espace
    }

    private void addGeneralInstructions(Document document) throws DocumentException {
        Paragraph instructionsTitle = new Paragraph("INSTRUCTIONS GÉNÉRALES", subTitle_Font);
        instructionsTitle.setSpacingBefore(10);
        instructionsTitle.setSpacingAfter(5);
        document.add(instructionsTitle);
        
        PdfPTable instructionsTable = new PdfPTable(1);
        instructionsTable.setWidthPercentage(100);
        
        String[] instructions = {
            "• Respectez strictement les doses et horaires prescrits",
            "• Ne pas interrompre le traitement sans avis médical",
            "• Conservez les médicaments hors de portée des enfants",
            "• Évitez l'alcool pendant le traitement",
            "• Consultez immédiatement en cas d'effets secondaires",
            "• Rendez-vous de contrôle dans 7 jours"
        };
        
        for (String instruction : instructions) {
            PdfPCell cell = new PdfPCell(new Phrase(instruction, text_Font));
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPadding(2);
            instructionsTable.addCell(cell);
        }
        
        document.add(instructionsTable);
        document.add(new Paragraph(" ")); // Espace
    }

    private void addSignatureSection(Document document, Medecin medecin) throws DocumentException {
        PdfPTable signatureTable = new PdfPTable(2);
        signatureTable.setWidthPercentage(100);
        signatureTable.setWidths(new float[]{0.5f, 0.5f});
        
        // Signature du médecin
        PdfPCell signatureCell = new PdfPCell();
        signatureCell.setBorder(Rectangle.NO_BORDER);
        signatureCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        Paragraph signature = new Paragraph();
        signature.add(new Chunk("Signature du Médecin\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK)));
        signature.add(new Chunk("_________________________\n", text_Font));
        signature.add(new Chunk("Dr. " + medecin.getNom() + " " + medecin.getPrenom() + "\n", text_Font));
        signature.add(new Chunk(medecin.getSpecialite() + "\n", text_Font));
        signature.add(new Chunk("N° RPPS: " + medecin.getId(), text_Font));
        
        signatureCell.addElement(signature);
        
        // Cachet et date
        PdfPCell stampCell = new PdfPCell();
        stampCell.setBorder(Rectangle.NO_BORDER);
        stampCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        Paragraph stamp = new Paragraph();
        stamp.add(new Chunk("Cachet de la Clinique\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK)));
        stamp.add(new Chunk("_________________________\n", text_Font));
        stamp.add(new Chunk("Date: " + java.time.LocalDate.now().toString(), text_Font));
        
        stampCell.addElement(stamp);
        
        signatureTable.addCell(signatureCell);
        signatureTable.addCell(stampCell);
        
        document.add(signatureTable);
    }

    private void addFooter(Document document) throws DocumentException {
        document.add(new Paragraph(" ")); // Espace
        
        LineSeparator footerSeparator = new LineSeparator();
        footerSeparator.setLineWidth(0.5f);
        footerSeparator.setLineColor(BaseColor.GRAY);
        document.add(footerSeparator);
        
        Paragraph footer = new Paragraph();
        footer.add(new Chunk("Ce document est généré automatiquement par le système de gestion de la clinique.\n", 
                FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY)));
        footer.add(new Chunk("Pour toute question, contactez votre médecin traitant.", 
                FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY)));
        footer.setAlignment(Element.ALIGN_CENTER);
        
        document.add(footer);
    }

    // Méthodes utilitaires pour créer les cellules
    private PdfPCell createLabelCell(String label) {
        PdfPCell cell = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK)));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private PdfPCell createValueCell(String value) {
        PdfPCell cell = new PdfPCell(new Phrase(value, text_Font));
        cell.setPadding(5);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private PdfPCell createMedicationCell(String content) {
        PdfPCell cell = new PdfPCell(new Phrase(content, text_Font2));
        cell.setPadding(5);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    // Méthodes utilitaires pour la facture
    private PdfPCell createServiceCell(String content) {
        PdfPCell cell = new PdfPCell(new Phrase(content, text_Font2));
        cell.setPadding(5);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private PdfPCell createSummaryLabelCell(String label) {
        PdfPCell cell = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK)));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }

    private PdfPCell createSummaryValueCell(String value) {
        PdfPCell cell = new PdfPCell(new Phrase(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK)));
        cell.setPadding(5);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }
}
