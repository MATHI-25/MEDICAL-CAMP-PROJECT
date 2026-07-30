package com.mediq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateConsultationRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Camp ID is required")
    private Long campId;

    @NotNull(message = "Queue Token ID is required")
    private Long queueTokenId;

    private Long vitalsId;

    private String diseaseName;

    @NotBlank(message = "Diagnosis notes are required")
    private String diagnosisNotes;

    private String labTestRecommendations;

    private String doctorNotes;

    private LocalDate followUpDate;

    @Builder.Default
    private Boolean requiresReferral = false;
}
