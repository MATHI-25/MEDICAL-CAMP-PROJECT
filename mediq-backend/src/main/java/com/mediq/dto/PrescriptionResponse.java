package com.mediq.dto;

import com.mediq.constants.PrescriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionResponse {

    private Long id;
    private String prescriptionCode;
    private Long consultationId;
    private String consultationCode;
    private PatientResponse patient;
    private UserResponse doctor;
    private Long campId;
    private String campTitle;
    private PrescriptionStatus status;
    private String generalInstructions;
    private String doctorSignature;
    private List<PrescriptionItemDto> items;
    private LocalDateTime createdAt;
}
