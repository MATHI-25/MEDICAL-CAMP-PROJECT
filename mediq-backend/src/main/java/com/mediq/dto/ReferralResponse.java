package com.mediq.dto;

import com.mediq.constants.ReferralStatus;
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
public class ReferralResponse {

    private Long id;
    private String referralId;
    private PatientResponse patient;
    private UserResponse doctor;
    private Long campId;
    private String campTitle;
    private Long consultationId;
    private String consultationCode;
    private String reason;
    private String recommendedTests;
    private String hospitalName;
    private String hospitalAddress;
    private String department;
    private String specialistType;
    private String doctorNotes;
    private String currentMedicines;
    private String urgency;
    private LocalDate followUpDate;
    private String remarks;
    private ReferralStatus status;
    private LocalDateTime createdAt;
}
