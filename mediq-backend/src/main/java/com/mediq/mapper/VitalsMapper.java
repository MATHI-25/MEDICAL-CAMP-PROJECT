package com.mediq.mapper;

import com.mediq.dto.PatientVitalsResponse;
import com.mediq.entity.PatientVitals;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VitalsMapper {

    private final UserMapper userMapper;

    public PatientVitalsResponse toResponse(PatientVitals vitals) {
        if (vitals == null) {
            return null;
        }

        return PatientVitalsResponse.builder()
                .id(vitals.getId())
                .patientId(vitals.getPatient() != null ? vitals.getPatient().getId() : null)
                .patientName(vitals.getPatient() != null ? vitals.getPatient().getFullName() : null)
                .queueTokenId(vitals.getQueueToken() != null ? vitals.getQueueToken().getId() : null)
                .tokenNumber(vitals.getQueueToken() != null ? vitals.getQueueToken().getTokenNumber() : null)
                .campId(vitals.getCamp() != null ? vitals.getCamp().getId() : null)
                .recordedByNurse(userMapper.toResponse(vitals.getRecordedByNurse()))
                .heightCm(vitals.getHeightCm())
                .weightKg(vitals.getWeightKg())
                .bmi(vitals.getBmi())
                .temperatureF(vitals.getTemperatureF())
                .bloodPressure(vitals.getBloodPressure())
                .pulseRate(vitals.getPulseRate())
                .respiratoryRate(vitals.getRespiratoryRate())
                .bloodSugarMgDl(vitals.getBloodSugarMgDl())
                .spo2Percent(vitals.getSpo2Percent())
                .symptoms(vitals.getSymptoms())
                .painScale(vitals.getPainScale())
                .nurseNotes(vitals.getNurseNotes())
                .createdAt(vitals.getCreatedAt())
                .build();
    }
}
