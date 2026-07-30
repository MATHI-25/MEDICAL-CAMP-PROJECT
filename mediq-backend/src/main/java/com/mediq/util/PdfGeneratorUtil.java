package com.mediq.util;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.mediq.entity.HospitalReferral;
import com.mediq.entity.Prescription;
import com.mediq.entity.PrescriptionItem;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Component
public class PdfGeneratorUtil {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
    private static final Font BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
    private static final Font REGULAR_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final Font SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.ITALIC, Color.GRAY);
    private static final Font URGENT_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(185, 28, 28));

    public byte[] generatePrescriptionPdf(Prescription prescription) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);

            document.open();

            // Header Section
            Paragraph title = new Paragraph("MediQ DIGITAL PRESCRIPTION", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph campInfo = new Paragraph(
                    String.format("%s | %s",
                            prescription.getCamp() != null ? prescription.getCamp().getTitle() : "Medical Camp",
                            prescription.getCamp() != null ? prescription.getCamp().getLocation() : "Healthcare Centre"),
                    SUBTITLE_FONT
            );
            campInfo.setAlignment(Element.ALIGN_CENTER);
            campInfo.setSpacingAfter(15);
            document.add(campInfo);

            // Divider Line
            PdfPTable line = new PdfPTable(1);
            line.setWidthPercentage(100);
            PdfPCell lineCell = new PdfPCell(new Paragraph(""));
            lineCell.setBackgroundColor(new Color(15, 118, 110)); // Teal color
            lineCell.setFixedHeight(3);
            lineCell.setBorder(Rectangle.NO_BORDER);
            line.addCell(lineCell);
            line.setSpacingAfter(15);
            document.add(line);

            // Patient & Doctor Information Table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1, 1});

            // Patient Details (Left Column)
            PdfPCell patientCell = new PdfPCell();
            patientCell.setBorder(Rectangle.NO_BORDER);
            patientCell.addElement(new Paragraph("PATIENT DETAILS", BOLD_FONT));
            patientCell.addElement(new Paragraph("Patient ID: " + prescription.getPatient().getPatientId(), REGULAR_FONT));
            patientCell.addElement(new Paragraph("Name: " + prescription.getPatient().getFullName(), REGULAR_FONT));
            patientCell.addElement(new Paragraph("Age / Gender: " + prescription.getPatient().getAge() + " Yrs / " + prescription.getPatient().getGender(), REGULAR_FONT));
            patientCell.addElement(new Paragraph("Blood Group: " + (prescription.getPatient().getBloodGroup() != null ? prescription.getPatient().getBloodGroup() : "N/A"), REGULAR_FONT));
            if (prescription.getPatient().getAllergies() != null && !prescription.getPatient().getAllergies().isBlank()) {
                patientCell.addElement(new Paragraph("Allergies: " + prescription.getPatient().getAllergies(), SMALL_FONT));
            }
            infoTable.addCell(patientCell);

            // Doctor & Prescription Details (Right Column)
            PdfPCell doctorCell = new PdfPCell();
            doctorCell.setBorder(Rectangle.NO_BORDER);
            doctorCell.addElement(new Paragraph("PRESCRIPTION DETAILS", BOLD_FONT));
            doctorCell.addElement(new Paragraph("Prescription Code: " + prescription.getPrescriptionCode(), REGULAR_FONT));
            doctorCell.addElement(new Paragraph("Date: " + prescription.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), REGULAR_FONT));
            doctorCell.addElement(new Paragraph("Prescribing Doctor: " + prescription.getDoctor().getFullName(), REGULAR_FONT));
            doctorCell.addElement(new Paragraph("Specialization: " + (prescription.getDoctor().getSpecialization() != null ? prescription.getDoctor().getSpecialization() : "General Medicine"), REGULAR_FONT));
            infoTable.addCell(doctorCell);

            infoTable.setSpacingAfter(20);
            document.add(infoTable);

            // Medicine Table Header
            PdfPTable medTable = new PdfPTable(6);
            medTable.setWidthPercentage(100);
            medTable.setWidths(new float[]{0.5f, 2.5f, 1f, 1.2f, 1f, 1.5f});

            addTableHeader(medTable, "#");
            addTableHeader(medTable, "Medicine Name");
            addTableHeader(medTable, "Dosage");
            addTableHeader(medTable, "Frequency");
            addTableHeader(medTable, "Duration");
            addTableHeader(medTable, "Instructions");

            int count = 1;
            for (PrescriptionItem item : prescription.getItems()) {
                medTable.addCell(new PdfPCell(new Phrase(String.valueOf(count++), REGULAR_FONT)));
                medTable.addCell(new PdfPCell(new Phrase(item.getMedicineName(), BOLD_FONT)));
                medTable.addCell(new PdfPCell(new Phrase(item.getDosage(), REGULAR_FONT)));
                medTable.addCell(new PdfPCell(new Phrase(item.getFrequency(), REGULAR_FONT)));
                medTable.addCell(new PdfPCell(new Phrase(item.getDuration(), REGULAR_FONT)));
                medTable.addCell(new PdfPCell(new Phrase(item.getInstructions() != null ? item.getInstructions() : "-", REGULAR_FONT)));
            }

            medTable.setSpacingAfter(20);
            document.add(medTable);

            // General Instructions / Doctor Notes
            if (prescription.getGeneralInstructions() != null && !prescription.getGeneralInstructions().isBlank()) {
                Paragraph instTitle = new Paragraph("Doctor Instructions:", BOLD_FONT);
                document.add(instTitle);
                Paragraph instBody = new Paragraph(prescription.getGeneralInstructions(), REGULAR_FONT);
                instBody.setSpacingAfter(20);
                document.add(instBody);
            }

            // Footer Signature Block
            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            footerTable.setWidths(new float[]{1, 1});

            PdfPCell stampCell = new PdfPCell();
            stampCell.setBorder(Rectangle.NO_BORDER);
            stampCell.addElement(new Paragraph("System Verified Digital Timestamp", SMALL_FONT));
            stampCell.addElement(new Paragraph("Valid at MediQ Pharmacy Outlets", SMALL_FONT));
            footerTable.addCell(stampCell);

            PdfPCell sigCell = new PdfPCell();
            sigCell.setBorder(Rectangle.NO_BORDER);
            sigCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            sigCell.addElement(new Paragraph("__________________________", BOLD_FONT));
            sigCell.addElement(new Paragraph("Digitally Signed by " + prescription.getDoctor().getFullName(), BOLD_FONT));
            sigCell.addElement(new Paragraph("Member ID: " + prescription.getDoctor().getMemberId(), SMALL_FONT));
            footerTable.addCell(sigCell);

            document.add(footerTable);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate digital prescription PDF", e);
        }
    }

    public byte[] generateReferralLetterPdf(HospitalReferral referral) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);

            document.open();

            // Header Section
            Paragraph title = new Paragraph("OFFICIAL HOSPITAL REFERRAL LETTER", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph campInfo = new Paragraph(
                    String.format("Issued by: %s (Camp Code: %s)",
                            referral.getCamp() != null ? referral.getCamp().getTitle() : "Medical Camp",
                            referral.getCamp() != null ? referral.getCamp().getCampCode() : "CAMP-001"),
                    SUBTITLE_FONT
            );
            campInfo.setAlignment(Element.ALIGN_CENTER);
            campInfo.setSpacingAfter(15);
            document.add(campInfo);

            // Divider Line
            PdfPTable line = new PdfPTable(1);
            line.setWidthPercentage(100);
            PdfPCell lineCell = new PdfPCell(new Paragraph(""));
            lineCell.setBackgroundColor(new Color(185, 28, 28)); // Dark Red color for referral
            lineCell.setFixedHeight(3);
            lineCell.setBorder(Rectangle.NO_BORDER);
            line.addCell(lineCell);
            line.setSpacingAfter(15);
            document.add(line);

            // Referral Header & Destination Info
            PdfPTable refTable = new PdfPTable(2);
            refTable.setWidthPercentage(100);
            refTable.setWidths(new float[]{1, 1});

            PdfPCell destCell = new PdfPCell();
            destCell.setBorder(Rectangle.NO_BORDER);
            destCell.addElement(new Paragraph("TO DESTINATION HOSPITAL:", BOLD_FONT));
            destCell.addElement(new Paragraph("Hospital: " + referral.getHospitalName(), BOLD_FONT));
            destCell.addElement(new Paragraph("Department: " + referral.getDepartment(), REGULAR_FONT));
            if (referral.getSpecialistType() != null) {
                destCell.addElement(new Paragraph("Specialist: " + referral.getSpecialistType(), REGULAR_FONT));
            }
            if (referral.getHospitalAddress() != null) {
                destCell.addElement(new Paragraph("Address: " + referral.getHospitalAddress(), SMALL_FONT));
            }
            refTable.addCell(destCell);

            PdfPCell metaCell = new PdfPCell();
            metaCell.setBorder(Rectangle.NO_BORDER);
            metaCell.addElement(new Paragraph("REFERRAL METADATA:", BOLD_FONT));
            metaCell.addElement(new Paragraph("Referral ID: " + referral.getReferralId(), BOLD_FONT));
            metaCell.addElement(new Paragraph("Date: " + referral.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), REGULAR_FONT));
            metaCell.addElement(new Paragraph("Urgency: " + referral.getUrgency().toUpperCase(), URGENT_FONT));
            metaCell.addElement(new Paragraph("Referring Doctor: " + referral.getDoctor().getFullName(), REGULAR_FONT));
            refTable.addCell(metaCell);

            refTable.setSpacingAfter(20);
            document.add(refTable);

            // Patient Information Section
            Paragraph patHeader = new Paragraph("PATIENT INFORMATION:", BOLD_FONT);
            document.add(patHeader);

            PdfPTable patTable = new PdfPTable(2);
            patTable.setWidthPercentage(100);
            patTable.setWidths(new float[]{1, 1});

            PdfPCell pLeft = new PdfPCell();
            pLeft.setBorder(Rectangle.NO_BORDER);
            pLeft.addElement(new Paragraph("Patient ID: " + referral.getPatient().getPatientId(), REGULAR_FONT));
            pLeft.addElement(new Paragraph("Full Name: " + referral.getPatient().getFullName(), REGULAR_FONT));
            pLeft.addElement(new Paragraph("Age / Gender: " + referral.getPatient().getAge() + " Yrs / " + referral.getPatient().getGender(), REGULAR_FONT));
            patTable.addCell(pLeft);

            PdfPCell pRight = new PdfPCell();
            pRight.setBorder(Rectangle.NO_BORDER);
            pRight.addElement(new Paragraph("Phone: " + (referral.getPatient().getPhone() != null ? referral.getPatient().getPhone() : "N/A"), REGULAR_FONT));
            pRight.addElement(new Paragraph("Emergency Contact: " + (referral.getPatient().getEmergencyContact() != null ? referral.getPatient().getEmergencyContact() : "N/A"), REGULAR_FONT));
            pRight.addElement(new Paragraph("Allergies: " + (referral.getPatient().getAllergies() != null ? referral.getPatient().getAllergies() : "None"), SMALL_FONT));
            patTable.addCell(pRight);

            patTable.setSpacingAfter(15);
            document.add(patTable);

            // Referral Reason & Clinical Findings
            Paragraph reasonTitle = new Paragraph("REASON FOR REFERRAL:", BOLD_FONT);
            document.add(reasonTitle);
            Paragraph reasonBody = new Paragraph(referral.getReason(), REGULAR_FONT);
            reasonBody.setSpacingAfter(15);
            document.add(reasonBody);

            if (referral.getRecommendedTests() != null && !referral.getRecommendedTests().isBlank()) {
                Paragraph testTitle = new Paragraph("RECOMMENDED LAB / DIAGNOSTIC TESTS:", BOLD_FONT);
                document.add(testTitle);
                Paragraph testBody = new Paragraph(referral.getRecommendedTests(), REGULAR_FONT);
                testBody.setSpacingAfter(15);
                document.add(testBody);
            }

            if (referral.getDoctorNotes() != null && !referral.getDoctorNotes().isBlank()) {
                Paragraph notesTitle = new Paragraph("CLINICAL NOTES & PRESENTING SYMPTOMS:", BOLD_FONT);
                document.add(notesTitle);
                Paragraph notesBody = new Paragraph(referral.getDoctorNotes(), REGULAR_FONT);
                notesBody.setSpacingAfter(15);
                document.add(notesBody);
            }

            // Footer Signature Block
            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            footerTable.setWidths(new float[]{1, 1});

            PdfPCell stampCell = new PdfPCell();
            stampCell.setBorder(Rectangle.NO_BORDER);
            stampCell.addElement(new Paragraph("Official Hospital Referral Document", SMALL_FONT));
            stampCell.addElement(new Paragraph("MediQ Digital Health Ecosystem", SMALL_FONT));
            footerTable.addCell(stampCell);

            PdfPCell sigCell = new PdfPCell();
            sigCell.setBorder(Rectangle.NO_BORDER);
            sigCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            sigCell.addElement(new Paragraph("__________________________", BOLD_FONT));
            sigCell.addElement(new Paragraph("Referring Doctor Signature", BOLD_FONT));
            sigCell.addElement(new Paragraph(referral.getDoctor().getFullName() + " (Member ID: " + referral.getDoctor().getMemberId() + ")", SMALL_FONT));
            footerTable.addCell(sigCell);

            document.add(footerTable);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate hospital referral PDF letter", e);
        }
    }

    private void addTableHeader(PdfPTable table, String headerTitle) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(new Color(15, 118, 110)); // Teal color
        header.setPhrase(new Phrase(headerTitle, HEADER_FONT));
        header.setPadding(6);
        table.addCell(header);
    }
}
