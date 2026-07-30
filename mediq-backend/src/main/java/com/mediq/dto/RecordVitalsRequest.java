package com.mediq.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordVitalsRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Queue Token ID is required")
    private Long queueTokenId;

    @NotNull(message = "Camp ID is required")
    private Long campId;

    private Long assignedDoctorId;

    private Double heightCm;

    private Double weightKg;

    private Double temperatureF;

    private String bloodPressure;

    private Integer pulseRate;

    private Integer respiratoryRate;

    private Double bloodSugarMgDl;

    private Integer spo2Percent;

    private String symptoms;

    @Min(value = 0, message = "Pain scale must be at least 0")
    @Max(value = 10, message = "Pain scale cannot exceed 10")
    private Integer painScale;

    private String nurseNotes;
}
