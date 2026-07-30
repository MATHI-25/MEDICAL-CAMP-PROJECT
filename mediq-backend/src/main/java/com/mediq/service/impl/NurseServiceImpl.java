package com.mediq.service.impl;

import com.mediq.constants.QueueStatus;
import com.mediq.constants.UserRole;
import com.mediq.dto.PatientVitalsResponse;
import com.mediq.dto.RecordVitalsRequest;
import com.mediq.entity.*;
import com.mediq.exception.ResourceNotFoundException;
import com.mediq.mapper.VitalsMapper;
import com.mediq.repository.*;
import com.mediq.service.MedicalHistoryService;
import com.mediq.service.NurseService;
import com.mediq.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NurseServiceImpl implements NurseService {

    private final PatientVitalsRepository vitalsRepository;
    private final PatientRepository patientRepository;
    private final QueueTokenRepository queueTokenRepository;
    private final CampRepository campRepository;
    private final UserRepository userRepository;
    private final VitalsMapper vitalsMapper;
    private final MedicalHistoryService medicalHistoryService;

    @Override
    @Transactional
    public PatientVitalsResponse recordVitals(RecordVitalsRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", request.getPatientId()));

        QueueToken queueToken = queueTokenRepository.findById(request.getQueueTokenId())
                .filter(q -> !q.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("QueueToken", "id", request.getQueueTokenId()));

        MedicalCamp camp = campRepository.findById(request.getCampId())
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MedicalCamp", "id", request.getCampId()));

        String currentMemberId = SecurityUtils.getCurrentUserMemberId()
                .orElseThrow(() -> new ResourceNotFoundException("Logged in user context missing"));

        User nurse = userRepository.findByMemberIdAndIsDeletedFalse(currentMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "memberId", currentMemberId));

        Double calculatedBmi = calculateBmi(request.getHeightCm(), request.getWeightKg());

        PatientVitals vitals = PatientVitals.builder()
                .patient(patient)
                .queueToken(queueToken)
                .camp(camp)
                .recordedByNurse(nurse)
                .heightCm(request.getHeightCm())
                .weightKg(request.getWeightKg())
                .bmi(calculatedBmi)
                .temperatureF(request.getTemperatureF())
                .bloodPressure(request.getBloodPressure())
                .pulseRate(request.getPulseRate())
                .respiratoryRate(request.getRespiratoryRate())
                .bloodSugarMgDl(request.getBloodSugarMgDl())
                .spo2Percent(request.getSpo2Percent())
                .symptoms(request.getSymptoms())
                .painScale(request.getPainScale() != null ? request.getPainScale() : 0)
                .nurseNotes(request.getNurseNotes())
                .build();

        PatientVitals savedVitals = vitalsRepository.save(vitals);

        // Update queue token status to WAITING_FOR_DOCTOR
        queueToken.setStatus(QueueStatus.WAITING_FOR_DOCTOR);
        if (request.getAssignedDoctorId() != null) {
            User doctor = userRepository.findById(request.getAssignedDoctorId())
                    .filter(u -> !u.isDeleted() && u.getRole() == UserRole.DOCTOR)
                    .orElse(null);
            if (doctor != null) {
                queueToken.setAssignedDoctor(doctor);
            }
        }
        queueTokenRepository.save(queueToken);

        // Record event in patient medical history
        medicalHistoryService.recordEvent(
                patient,
                "VITALS",
                String.format("Vitals recorded (BP: %s, Temp: %s°F, SpO2: %s%%)",
                        request.getBloodPressure() != null ? request.getBloodPressure() : "N/A",
                        request.getTemperatureF() != null ? request.getTemperatureF().toString() : "N/A",
                        request.getSpo2Percent() != null ? request.getSpo2Percent().toString() : "N/A"),
                String.format("BMI: %s, Pulse: %s bpm, Sugar: %s mg/dL. Nurse Notes: %s",
                        calculatedBmi != null ? calculatedBmi.toString() : "N/A",
                        request.getPulseRate() != null ? request.getPulseRate().toString() : "N/A",
                        request.getBloodSugarMgDl() != null ? request.getBloodSugarMgDl().toString() : "N/A",
                        request.getNurseNotes() != null ? request.getNurseNotes() : "None"),
                queueToken.getTokenNumber(),
                nurse.getFullName()
        );

        return vitalsMapper.toResponse(savedVitals);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientVitalsResponse getVitalsById(Long vitalsId) {
        PatientVitals vitals = vitalsRepository.findById(vitalsId)
                .filter(v -> !v.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("PatientVitals", "id", vitalsId));
        return vitalsMapper.toResponse(vitals);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientVitalsResponse getVitalsByTokenId(Long queueTokenId) {
        PatientVitals vitals = vitalsRepository.findByQueueTokenIdAndIsDeletedFalse(queueTokenId)
                .orElseThrow(() -> new ResourceNotFoundException("PatientVitals", "queueTokenId", queueTokenId));
        return vitalsMapper.toResponse(vitals);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientVitalsResponse> getPatientVitalsHistory(Long patientId) {
        return vitalsRepository.findByPatientIdAndIsDeletedFalseOrderByCreatedAtDesc(patientId)
                .stream()
                .map(vitalsMapper::toResponse)
                .collect(Collectors.toList());
    }

    private Double calculateBmi(Double heightCm, Double weightKg) {
        if (heightCm == null || weightKg == null || heightCm <= 0 || weightKg <= 0) {
            return null;
        }

        double heightMeters = heightCm / 100.0;
        double bmi = weightKg / (heightMeters * heightMeters);

        return BigDecimal.valueOf(bmi)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
