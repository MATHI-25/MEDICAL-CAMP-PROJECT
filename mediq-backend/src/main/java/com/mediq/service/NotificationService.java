package com.mediq.service;

import com.mediq.dto.SmsNotificationRequest;
import com.mediq.dto.SmsNotificationResponse;

public interface NotificationService {

    SmsNotificationResponse sendRegistrationSms(Long patientId, Long campId);

    SmsNotificationResponse sendPrescriptionSms(Long prescriptionId);

    SmsNotificationResponse sendCustomSms(SmsNotificationRequest request);

    SmsNotificationResponse sendWhatsApp(SmsNotificationRequest request);
}
