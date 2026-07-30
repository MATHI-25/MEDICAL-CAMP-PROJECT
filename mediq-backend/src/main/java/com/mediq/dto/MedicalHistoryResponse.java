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
public class MedicalHistoryResponse {

    private Long id;
    private Long patientId;
    private String eventType;
    private String eventSummary;
    private String eventDetails;
    private String referenceCode;
    private String performedBy;
    private LocalDateTime eventTimestamp;
}
