package com.mediq.controller;

import com.mediq.dto.ApiResponse;
import com.mediq.dto.SmsNotificationRequest;
import com.mediq.dto.SmsNotificationResponse;
import com.mediq.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "SMS & WhatsApp Notification Module", description = "APIs for sending registration SMS receipts, plain-text Keypad Phone prescription SMS, and WhatsApp messages")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send-registration-sms")
    @Operation(summary = "Send Camp Registration & Token SMS", description = "Sends cellular SMS with camp venue location, operating timing, and token receipt to patient mobile number.")
    public ResponseEntity<ApiResponse<SmsNotificationResponse>> sendRegistrationSms(
            @RequestParam Long patientId,
            @RequestParam Long campId) {
        SmsNotificationResponse response = notificationService.sendRegistrationSms(patientId, campId);
        return ResponseEntity.ok(ApiResponse.success(response, "Registration SMS dispatched successfully to patient mobile number"));
    }

    @PostMapping("/send-prescription-sms/{prescriptionId}")
    @Operation(summary = "Send Keypad Phone Plain-Text Prescription SMS", description = "Dispatches plain-text prescription summary (medicines, dosages, instructions) to patient mobile number suitable for basic keypad phones.")
    public ResponseEntity<ApiResponse<SmsNotificationResponse>> sendPrescriptionSms(@PathVariable Long prescriptionId) {
        SmsNotificationResponse response = notificationService.sendPrescriptionSms(prescriptionId);
        return ResponseEntity.ok(ApiResponse.success(response, "Plain-text prescription SMS dispatched successfully to patient mobile number"));
    }

    @PostMapping("/send-custom-sms")
    @Operation(summary = "Send Custom SMS Notification", description = "Sends custom SMS payload.")
    public ResponseEntity<ApiResponse<SmsNotificationResponse>> sendCustomSms(@RequestBody SmsNotificationRequest request) {
        SmsNotificationResponse response = notificationService.sendCustomSms(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Custom notification dispatched successfully"));
    }

    @PostMapping("/send-whatsapp")
    @Operation(summary = "Send WhatsApp Message via API Gateway", description = "Dispatches real physical WhatsApp message to recipient phone number using configured WhatsApp API Gateway (Twilio or Meta).")
    public ResponseEntity<ApiResponse<SmsNotificationResponse>> sendWhatsApp(@RequestBody SmsNotificationRequest request) {
        SmsNotificationResponse response = notificationService.sendWhatsApp(request);
        return ResponseEntity.ok(ApiResponse.success(response, "WhatsApp message dispatched successfully to recipient mobile number"));
    }
}
