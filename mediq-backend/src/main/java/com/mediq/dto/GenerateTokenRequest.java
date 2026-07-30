package com.mediq.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateTokenRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Camp ID is required")
    private Long campId;

    private Long assignedDoctorId;
}
