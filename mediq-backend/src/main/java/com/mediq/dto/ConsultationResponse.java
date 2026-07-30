package com.mediq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationResponse {

    private Long id;
    private String consultationCode;
    private PatientResponse patient;
    private UserResponse doctor;
    private Long campId;
    private String campTitle;
    private Long queueTokenId;
    private String tokenNumber;
    private PatientVitalsResponse vitals;
    private String diseaseName;
    private String diagnosisNotes;
    private String labTestRecommendations;
    private String doctorNotes;
    private LocalDate followUpDate;
    private Boolean requiresReferral;
    private LocalDateTime createdAt;
}
