package org.kfokam48.cliniquemanagementbackend.service.pdf;


import com.itextpdf.text.*;

import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.kfokam48.cliniquemanagementbackend.model.Facture;
import org.kfokam48.cliniquemanagementbackend.model.Medecin;
import org.kfokam48.cliniquemanagementbackend.model.Patient;
import org.kfokam48.cliniquemanagementbackend.model.Prescription;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;


@Service
public class PdfService {
    private final Font title_Font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 30, BaseColor.BLUE);
    private final Font subTitle_Font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLUE);
    private final Font text_Font = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
    private final Font text_Font2 = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);


    public ByteArrayOutputStream generateFacturePdf(Facture facture) throws DocumentException {


        Document document = new Document();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter pdfWriter = PdfWriter.getInstance(document, outputStream);
        document.open();
        // Titre du document
        Paragraph title = new Paragraph("Facture Médicale");
        title.setAlignment(Element.ALIGN_CENTER);
        title.setFont(title_Font);
        title.setSpacingAfter(10);
      //  title.setSpacingBefore(10);
        document.add(title);
        document.add(new Paragraph("========================================================================"));

        // Contenu de la facture
        PdfPTable factureTable = new PdfPTable(2);
        float[] columnWidths = {250, 150F, 150F};
        factureTable.addCell(new PdfPCell(new Phrase("Numéro de Facture: "))).setMinimumHeight(30);
        factureTable.addCell(new PdfPCell(new Phrase(String.valueOf(facture.getId())))).setMinimumHeight(30);
        factureTable.addCell(new PdfPCell(new Phrase("Date d'émission: "))).setMinimumHeight(30);
        factureTable.addCell(new PdfPCell(new Phrase(facture.getDateEmission().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))))).setMinimumHeight(30);
        factureTable.addCell(new PdfPCell(new Phrase("Patient: ")));
        factureTable.addCell(new PdfPCell(new Phrase(facture.getRendezVous().getPatient().getNom() + " " + facture.getRendezVous().getPatient().getPrenom()))).setMinimumHeight(30);
        factureTable.addCell(new PdfPCell(new Phrase("Médecin: ")));
        factureTable.addCell(new PdfPCell(new Phrase(facture.getRendezVous().getMedecin().getNom() + " " + facture.getRendezVous().getMedecin().getPrenom()))).setMinimumHeight(30);
        factureTable.addCell(new PdfPCell(new Phrase("Montant Total: ")));
        factureTable.addCell(new PdfPCell(new Phrase(facture.getMontantTotal() + " €"))).setMinimumHeight(30);
        factureTable.addCell(new PdfPCell(new Phrase("Montant Versé: ")));
        factureTable.addCell(new PdfPCell(new Phrase(facture.getMontantPayement() + " €"))).setMinimumHeight(30);
        factureTable.addCell(new PdfPCell(new Phrase("Montant Restant: ")));
        factureTable.addCell(new PdfPCell(new Phrase(facture.getMontantRestant() + " €"))).setMinimumHeight(30);
        factureTable.addCell(new PdfPCell(new Phrase("Mode de Paiement: ")));
        factureTable.addCell(new PdfPCell(new Phrase(facture.getModePayement() != null ? facture.getModePayement().toString() : "Non spécifié"))).setMinimumHeight(30);
        factureTable.addCell(new PdfPCell(new Phrase("Statut de Paiement: ")));
        factureTable.addCell(new PdfPCell(new Phrase(facture.getStatutPayement() != null ? facture.getStatutPayement().toString() : "Non spécifié"))).setMinimumHeight(30);
        factureTable.addCell(new PdfPCell(new Phrase("Date de Paiement: ")));
        factureTable.addCell(new PdfPCell(new Phrase(facture.getDatePayement() != null ? facture.getDatePayement().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Non spécifié"))).setMinimumHeight(30);
        factureTable.addCell(new PdfPCell(new Phrase("Description: ")));
        factureTable.addCell(new PdfPCell(new Phrase(facture.getDescription() != null ? facture.getDescription() : "Non spécifié"))).setMinimumHeight(30);
        factureTable.setSpacingAfter(40);
        factureTable.setSpacingBefore(30);

        factureTable.setHorizontalAlignment(Element.ALIGN_CENTER);

        document.add(factureTable);
        // Ajouter une ligne de séparation
        Paragraph signature = new Paragraph("Signature du Médecin: ____________________________");
        signature.setSpacingAfter(30);
       // signature.setSpacingBefore(30);
        signature.setAlignment(Element.ALIGN_RIGHT);
        document.add(signature);

        Paragraph signaturePatient = new Paragraph("Signature du Patient: ____________________________");
       // signaturePatient.setSpacingAfter(10);
        signaturePatient.setSpacingBefore(30);
        signaturePatient.setAlignment(Element.ALIGN_LEFT);
        document.add(signaturePatient);
        // Ajouter un message de remerciement
        Paragraph remerciement = new Paragraph();
        Paragraph line = new Paragraph("========================================================================");
        Paragraph line2 = new Paragraph("Merci de votre confiance !");
        Paragraph line3 = new Paragraph("Pour toute question, veuillez nous contacter à l'adresse suivante : +237 696784489");
        remerciement.add(line);
        remerciement.add(line2);
        remerciement.add(line3);
        remerciement.setAlignment(Element.ALIGN_BOTTOM);
        remerciement.setAlignment(Element.ALIGN_CENTER);
        remerciement.setSpacingBefore(60);
        document.add(remerciement);


        document.close();
        return outputStream;
    }

    public ByteArrayOutputStream generatePrescriptionPdf(Prescription prescription) throws DocumentException {
        Patient patient = prescription.getPatient();
        Medecin medecin = prescription.getMedecin();
        Document document = new Document();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter pdfWriter = PdfWriter.getInstance(document, outputStream);

        //PdfWriter.getInstance(document, outputStream);
        document.open();

        // ✅ Ajout de l'en-tête de la clinique
//        document.add(new Paragraph("######################################################################"));
//        Paragraph entete = new Paragraph("CLINIC NAME");
//        entete.setAlignment(Element.ALIGN_CENTER);
//        entete.setFont(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 30));
//        document.add(entete);
//        document.add(new Paragraph("Yaoundé , EMANA, CAMEROUN"));
//        document.add(new Paragraph("youremail@companyname.com / yourwebsite.com"));
//        document.add(new Paragraph("#######################################################################"));
        // Titre du document
        Paragraph title = new Paragraph("PRESCRIPTION MEDICALE");
        title.setAlignment(Element.ALIGN_CENTER);
        title.setFont(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLUE));
        title.setSpacingAfter(10);
        title.setSpacingBefore(10);
        document.add(title);
        document.add(new Paragraph("========================================================================"));
        Paragraph info = new Paragraph();

        // Ajouter la date de prescription
        Phrase prescriptionDate = new Phrase("Prescription Date: " + prescription.getDatePrescription().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        info.add(prescriptionDate);

        // Ajouter le numéro de prescription
        Phrase prescriptionNumber = new Phrase("Prescription Number: " + prescription.getId());
        prescriptionNumber.setFont(FontFactory.getFont(FontFactory.HELVETICA, 14));
        info.add(new Chunk("         /           ")); // Ajouter un séparateur entre les deux informations
        info.add(prescriptionNumber);
        document.add(info);





        Paragraph subTitle = new Paragraph("PATIENT INFORMATIONS");
        title.setFont(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
        subTitle.setSpacingAfter(5);
        subTitle.setSpacingBefore(15);
        subTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(subTitle);
        // Ajouter les informations du patient
        Paragraph patientInfo = new Paragraph();
        patientInfo.add(new Paragraph("Nom: " + (patient.getNom() !=null ? patient.getNom() : "N/A")));
        patientInfo.add(new Paragraph("Prénom: " + (patient.getPrenom() !=null ? patient.getPrenom() : "N/A")));
        patientInfo.add(new Paragraph("Numéro de dossier médical: " + (!patient.getNumeroDossierMedical().toString().isEmpty() ? patient.getNumeroDossierMedical() : "N/A")));
        patientInfo.add(new Paragraph("Phone Number: " +(patient.getTelephone() != null ? patient.getTelephone() : "N/A")));
        patientInfo.setAlignment(Element.ALIGN_LEFT);
        document.add(patientInfo);


        Paragraph patientInfo2 = new Paragraph();
        patientInfo2.add(new Paragraph("Date of Birth: "+ new Phrase(patient.getDateNaissance() != null ? patient.getDateNaissance().toString() : "N/A")));
        patientInfo2.add(new Paragraph("Email: " + (patient.getEmail() != null ? patient.getEmail() : "N/A")));
        patientInfo2.add(new Paragraph("Gender: "+ (patient.getSexe() != null ? patient.getSexe().toString() : "N/A")));
        patientInfo2.add(new Paragraph("Address: " + (patient.getAdresse() != null ? patient.getAdresse() : "N/A")));
        patientInfo2.add(new Paragraph("Allergies: " + (patient.getAllergies() != null ? patient.getAllergies() : "N/A")));
        patientInfo2.setAlignment(Element.ALIGN_RIGHT);
        document.add(patientInfo2);

        // Ajouter la liste des médicaments prescrits
        document.add(new Paragraph(" "));
        Paragraph subTitle2 = new Paragraph("List of Medications Prescribed");
        subTitle2.setFont(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
        subTitle2.setSpacingAfter(5);
        subTitle2.setAlignment(Element.ALIGN_CENTER);
        document.add(subTitle2);
        PdfPTable medicationTable = new PdfPTable(5);
        medicationTable.addCell(new PdfPCell(new Phrase("Medication Name")));
        medicationTable.addCell(new PdfPCell(new Phrase("Purpose")));
        medicationTable.addCell(new PdfPCell(new Phrase("Dosage")));
        medicationTable.addCell(new PdfPCell(new Phrase("Route")));
        medicationTable.addCell(new PdfPCell(new Phrase("Frequency")));

        // Exemple de médicaments
        medicationTable.addCell(new PdfPCell(new Phrase(prescription.getMedicaments())));
        medicationTable.addCell(new PdfPCell(new Phrase("Removes phlegm")));
        medicationTable.addCell(new PdfPCell(new Phrase("1 tablet")));
        medicationTable.addCell(new PdfPCell(new Phrase("Oral")));
        medicationTable.addCell(new PdfPCell(new Phrase(prescription.getInstructions() != null ? prescription.getInstructions() : "N/A")));
        // Ajouter les autres médicaments de la même manière
        document.add(medicationTable);

        // Ajouter les informations du médecin

        Paragraph subTitle3 = new Paragraph("MEDECIN INFORMATIONS");
        subTitle3.setFont(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
        subTitle3.setSpacingAfter(5);
        subTitle3.setSpacingBefore(15);
        subTitle3.setAlignment(Element.ALIGN_CENTER);
        document.add(subTitle3);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Nom: " + (medecin.getNom()!=null ? medecin.getNom() : "N/A")));
        document.add(new Paragraph("Prénom: " + (medecin.getPrenom() !=null ? medecin.getPrenom() : "N/A")));
        document.add(new Paragraph("Phone Number: " + (medecin.getTelephone()!= null ? medecin.getTelephone() : "N/A")));
        document.add(new Paragraph("Email: " + (medecin.getEmail() != null ? medecin.getEmail() : "N/A")));
        document.add(new Paragraph("Address: " + (medecin.getAdresse()!=null ? medecin.getAdresse() : "N/A")));
        document.add(new Paragraph("Specialty: " + (medecin.getSpecialite() != null ? medecin.getSpecialite() : "N/A")));
        document.add(new Paragraph(new Phrase("Physician Signature: [Signature]")));


//        PdfAppearance appearance = PdfAppearance.createAppearance(pdfWriter, 500, 200);
//        appearance.setColorFill(BaseColor.LIGHT_GRAY);

        document.close();
        return outputStream;
    }
}
