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
public class PatientResponse {

    private Long id;
    private String patientId;
    private String fullName;
    private Integer age;
    private String gender;
    private String bloodGroup;
    private String phone;
    private String address;
    private String emergencyContact;
    private String allergies;
    private String chronicDiseases;
    private Long registeredCampId;
    private String registeredCampTitle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
