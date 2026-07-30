package com.mediq.service.impl;

import com.mediq.dto.SmsNotificationRequest;
import com.mediq.dto.SmsNotificationResponse;
import com.mediq.entity.MedicalCamp;
import com.mediq.entity.Patient;
import com.mediq.entity.Prescription;
import com.mediq.entity.PrescriptionItem;
import com.mediq.entity.QueueToken;
import com.mediq.exception.ResourceNotFoundException;
import com.mediq.repository.CampRepository;
import com.mediq.repository.PatientRepository;
import com.mediq.repository.PrescriptionRepository;
import com.mediq.repository.QueueTokenRepository;
import com.mediq.service.SmsGatewayService;
import com.mediq.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import com.mediq.service.WhatsappGatewayService;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final PatientRepository patientRepository;
    private final CampRepository campRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final QueueTokenRepository queueTokenRepository;
    private final SmsGatewayService smsGatewayService;
    private final WhatsappGatewayService whatsappGatewayService;

    @Override
    @Transactional(readOnly = true)
    public SmsNotificationResponse sendRegistrationSms(Long patientId, Long campId) {
        Patient patient = patientRepository.findById(patientId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", patientId));

        MedicalCamp camp = campRepository.findById(campId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MedicalCamp", "id", campId));

        List<QueueToken> tokens = queueTokenRepository.findByPatientIdAndIsDeletedFalseOrderByCreatedAtDesc(patientId);
        String tokenNumber = !tokens.isEmpty() ? tokens.get(0).getTokenNumber() : "N/A";
        String timing = camp.getOperatingHours() != null ? camp.getOperatingHours() : "09:00 AM - 05:00 PM";

        StringBuilder sb = new StringBuilder();
        sb.append("[MediQ Camp Registration Receipt]\n");
        sb.append("Dear ").append(patient.getFullName()).append(",\n");
        sb.append("Registered for: ").append(camp.getTitle()).append("\n");
        sb.append("Location: ").append(camp.getLocation() != null ? camp.getLocation() : "Camp Venue").append("\n");
        sb.append("Timing: ").append(timing).append("\n");
        sb.append("Token No: ").append(tokenNumber).append(" | Patient ID: ").append(patient.getPatientId()).append("\n");
        sb.append("Please present this token at the screening station.");

        String messageBody = sb.toString();
        log.info("DISPATCHING CELLULAR SMS TO PHONE {}: \n{}", patient.getPhone(), messageBody);
        boolean dispatched = smsGatewayService.sendPhysicalSms(patient.getPhone(), messageBody);

        return SmsNotificationResponse.builder()
                .success(dispatched)
                .status(dispatched ? "SENT" : "QUEUED")
                .recipientPhone(patient.getPhone())
                .recipientName(patient.getFullName())
                .messageBody(messageBody)
                .notificationType("REGISTRATION_SMS")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SmsNotificationResponse sendPrescriptionSms(Long prescriptionId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", prescriptionId));

        Patient patient = prescription.getPatient();
        MedicalCamp camp = prescription.getCamp();

        StringBuilder sb = new StringBuilder();
        sb.append("[MediQ Health Camp Prescription]\n");
        sb.append("Rx Code: ").append(prescription.getPrescriptionCode()).append("\n");
        sb.append("Patient: ").append(patient != null ? patient.getFullName() : "N/A").append("\n");
        sb.append("Doctor: ").append(prescription.getDoctorSignature() != null ? prescription.getDoctorSignature() : "Camp Medical Officer").append("\n");
        sb.append("Medicines:\n");

        if (prescription.getItems() != null && !prescription.getItems().isEmpty()) {
            int count = 1;
            for (PrescriptionItem item : prescription.getItems()) {
                sb.append(count++).append(". ").append(item.getMedicineName());
                if (item.getDosage() != null) sb.append(" | ").append(item.getDosage());
                if (item.getFrequency() != null) sb.append(" | ").append(item.getFrequency());
                if (item.getDuration() != null) sb.append(" (").append(item.getDuration()).append(")");
                sb.append(" Qty: ").append(item.getQuantityPrescribed()).append("\n");
            }
        } else {
            sb.append("No medicines listed.\n");
        }

        if (prescription.getGeneralInstructions() != null && !prescription.getGeneralInstructions().isBlank()) {
            sb.append("Instructions: ").append(prescription.getGeneralInstructions()).append("\n");
        }
        sb.append("Camp: ").append(camp != null ? camp.getTitle() : "MediQ Camp");

        String messageBody = sb.toString();
        String targetPhone = patient != null ? patient.getPhone() : "+15550199";
        log.info("DISPATCHING KEYPAD PHONE PLAIN-TEXT SMS TO {}: \n{}", targetPhone, messageBody);
        boolean dispatched = smsGatewayService.sendPhysicalSms(targetPhone, messageBody);

        return SmsNotificationResponse.builder()
                .success(dispatched)
                .status(dispatched ? "SENT" : "QUEUED")
                .recipientPhone(targetPhone)
                .recipientName(patient != null ? patient.getFullName() : "N/A")
                .messageBody(messageBody)
                .notificationType("PRESCRIPTION_SMS")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SmsNotificationResponse sendCustomSms(SmsNotificationRequest request) {
        String phone = request.getPhoneNumber() != null ? request.getPhoneNumber() : "+1-555-0199";
        String messageBody = request.getCustomMessage() != null ? request.getCustomMessage() : "[MediQ Health Alert] Medical notification.";

        log.info("DISPATCHING CUSTOM SMS TO {}: {}", phone, messageBody);

        return SmsNotificationResponse.builder()
                .success(true)
                .status("SENT")
                .recipientPhone(phone)
                .recipientName("Patient")
                .messageBody(messageBody)
                .notificationType(request.getNotificationType() != null ? request.getNotificationType() : "CUSTOM_SMS")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SmsNotificationResponse sendWhatsApp(SmsNotificationRequest request) {
        String phone = request.getPhoneNumber() != null ? request.getPhoneNumber() : "+919876543210";
        String messageBody = request.getCustomMessage() != null ? request.getCustomMessage() : "[MediQ Health Alert] Medical WhatsApp Notification.";

        log.info("DISPATCHING PHYSICAL WHATSAPP TO {}: {}", phone, messageBody);
        boolean delivered = whatsappGatewayService.sendPhysicalWhatsapp(phone, messageBody);

        return SmsNotificationResponse.builder()
                .success(delivered)
                .status(delivered ? "SENT" : "FAILED")
                .recipientPhone(phone)
                .recipientName("Patient")
                .messageBody(messageBody)
                .notificationType("WHATSAPP_MESSAGE")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
