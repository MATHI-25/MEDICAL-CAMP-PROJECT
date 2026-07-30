package com.mediq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsNotificationRequest {

    private String phoneNumber;
    private Long patientId;
    private Long campId;
    private Long prescriptionId;
    private String customMessage;
    private String notificationType; // "REGISTRATION_SMS", "PRESCRIPTION_SMS", "WHATSAPP_RX"
}
