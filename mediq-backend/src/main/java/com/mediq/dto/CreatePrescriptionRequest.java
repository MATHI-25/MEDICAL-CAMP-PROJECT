package com.mediq.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePrescriptionRequest {

    @NotNull(message = "Consultation ID is required")
    private Long consultationId;

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Camp ID is required")
    private Long campId;

    private String generalInstructions;

    private String doctorSignature;

    @NotEmpty(message = "Prescription must contain at least one medicine item")
    @Valid
    private List<PrescriptionItemDto> items;
}
