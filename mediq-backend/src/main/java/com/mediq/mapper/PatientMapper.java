package com.mediq.mapper;

import com.mediq.dto.PatientResponse;
import com.mediq.entity.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    public PatientResponse toResponse(Patient patient) {
        if (patient == null) {
            return null;
        }

        return PatientResponse.builder()
                .id(patient.getId())
                .patientId(patient.getPatientId())
                .fullName(patient.getFullName())
                .age(patient.getAge())
                .gender(patient.getGender())
                .bloodGroup(patient.getBloodGroup())
                .phone(patient.getPhone())
                .address(patient.getAddress())
                .emergencyContact(patient.getEmergencyContact())
                .allergies(patient.getAllergies())
                .chronicDiseases(patient.getChronicDiseases())
                .registeredCampId(patient.getRegisteredCamp() != null ? patient.getRegisteredCamp().getId() : null)
                .registeredCampTitle(patient.getRegisteredCamp() != null ? patient.getRegisteredCamp().getTitle() : null)
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .build();
    }
}
