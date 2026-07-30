package com.mediq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsNotificationResponse {

    private boolean success;
    private String status;
    private String recipientPhone;
    private String recipientName;
    private String messageBody;
    private String notificationType;
    private LocalDateTime timestamp;
}
