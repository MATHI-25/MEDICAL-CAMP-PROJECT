package com.mediq.mapper;

import com.mediq.dto.PrescriptionItemDto;
import com.mediq.dto.PrescriptionResponse;
import com.mediq.entity.Prescription;
import com.mediq.entity.PrescriptionItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PrescriptionMapper {

    private final PatientMapper patientMapper;
    private final UserMapper userMapper;

    public PrescriptionResponse toResponse(Prescription prescription) {
        if (prescription == null) {
            return null;
        }

        return PrescriptionResponse.builder()
                .id(prescription.getId())
                .prescriptionCode(prescription.getPrescriptionCode())
                .consultationId(prescription.getConsultation() != null ? prescription.getConsultation().getId() : null)
                .consultationCode(prescription.getConsultation() != null ? prescription.getConsultation().getConsultationCode() : null)
                .patient(patientMapper.toResponse(prescription.getPatient()))
                .doctor(userMapper.toResponse(prescription.getDoctor()))
                .campId(prescription.getCamp() != null ? prescription.getCamp().getId() : null)
                .campTitle(prescription.getCamp() != null ? prescription.getCamp().getTitle() : null)
                .status(prescription.getStatus())
                .generalInstructions(prescription.getGeneralInstructions())
                .doctorSignature(prescription.getDoctorSignature())
                .items(prescription.getItems() != null ?
                        prescription.getItems().stream().map(this::toItemDto).collect(Collectors.toList()) : Collections.emptyList())
                .createdAt(prescription.getCreatedAt())
                .build();
    }

    public PrescriptionItemDto toItemDto(PrescriptionItem item) {
        if (item == null) {
            return null;
        }

        return PrescriptionItemDto.builder()
                .id(item.getId())
                .medicineName(item.getMedicineName())
                .dosage(item.getDosage())
                .frequency(item.getFrequency())
                .duration(item.getDuration())
                .instructions(item.getInstructions())
                .quantityPrescribed(item.getQuantityPrescribed())
                .quantityDispensed(item.getQuantityDispensed())
                .build();
    }
}
