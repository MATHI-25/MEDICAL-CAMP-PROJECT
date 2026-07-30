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
public class CreateReferralRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Camp ID is required")
    private Long campId;

    @NotNull(message = "Consultation ID is required")
    private Long consultationId;

    @NotBlank(message = "Reason for referral is required")
    private String reason;

    private String recommendedTests;

    @NotBlank(message = "Hospital name is required")
    private String hospitalName;

    private String hospitalAddress;

    @NotBlank(message = "Department is required")
    private String department;

    private String specialistType;

    private String doctorNotes;

    private String currentMedicines;

    @Builder.Default
    private String urgency = "NORMAL";

    private LocalDate followUpDate;

    private String remarks;
}
