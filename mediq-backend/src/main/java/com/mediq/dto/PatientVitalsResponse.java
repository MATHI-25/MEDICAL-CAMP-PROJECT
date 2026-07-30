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
public class PatientVitalsResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private Long queueTokenId;
    private String tokenNumber;
    private Long campId;
    private UserResponse recordedByNurse;
    private Double heightCm;
    private Double weightKg;
    private Double bmi;
    private Double temperatureF;
    private String bloodPressure;
    private Integer pulseRate;
    private Integer respiratoryRate;
    private Double bloodSugarMgDl;
    private Integer spo2Percent;
    private String symptoms;
    private Integer painScale;
    private String nurseNotes;
    private LocalDateTime createdAt;
}
