package com.mediq.service.impl;

import com.mediq.constants.QueueStatus;
import com.mediq.dto.ConsultationResponse;
import com.mediq.dto.CreateConsultationRequest;
import com.mediq.entity.*;
import com.mediq.exception.ResourceNotFoundException;
import com.mediq.mapper.ConsultationMapper;
import com.mediq.repository.*;
import com.mediq.service.DoctorService;
import com.mediq.service.MedicalHistoryService;
import com.mediq.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final ConsultationRepository consultationRepository;
    private final PatientRepository patientRepository;
    private final CampRepository campRepository;
    private final QueueTokenRepository queueTokenRepository;
    private final PatientVitalsRepository vitalsRepository;
    private final UserRepository userRepository;
    private final ConsultationMapper consultationMapper;
    private final MedicalHistoryService medicalHistoryService;

    @Override
    @Transactional
    public ConsultationResponse saveConsultation(CreateConsultationRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", request.getPatientId()));

        MedicalCamp camp = campRepository.findById(request.getCampId())
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MedicalCamp", "id", request.getCampId()));

        QueueToken queueToken = queueTokenRepository.findById(request.getQueueTokenId())
                .filter(q -> !q.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("QueueToken", "id", request.getQueueTokenId()));

        PatientVitals vitals = null;
        if (request.getVitalsId() != null) {
            vitals = vitalsRepository.findById(request.getVitalsId())
                    .filter(v -> !v.isDeleted())
                    .orElse(null);
        } else {
            vitals = vitalsRepository.findByQueueTokenIdAndIsDeletedFalse(queueToken.getId()).orElse(null);
        }

        String currentMemberId = SecurityUtils.getCurrentUserMemberId()
                .orElseThrow(() -> new ResourceNotFoundException("Logged in doctor session missing"));

        User doctor = userRepository.findByMemberIdAndIsDeletedFalse(currentMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "memberId", currentMemberId));

        Consultation existing = consultationRepository.findByQueueTokenIdAndIsDeletedFalse(queueToken.getId()).orElse(null);
        Consultation savedConsultation;
        String finalCode;

        if (existing != null) {
            existing.setDiseaseName(request.getDiseaseName());
            existing.setDiagnosisNotes(request.getDiagnosisNotes());
            existing.setLabTestRecommendations(request.getLabTestRecommendations());
            existing.setDoctorNotes(request.getDoctorNotes());
            existing.setFollowUpDate(request.getFollowUpDate());
            existing.setRequiresReferral(Boolean.TRUE.equals(request.getRequiresReferral()));
            if (vitals != null) existing.setVitals(vitals);
            savedConsultation = consultationRepository.save(existing);
            finalCode = existing.getConsultationCode();
        } else {
            String generatedCode = generateConsultationCode(camp.getId());
            Consultation consultation = Consultation.builder()
                    .consultationCode(generatedCode)
                    .patient(patient)
                    .doctor(doctor)
                    .camp(camp)
                    .queueToken(queueToken)
                    .vitals(vitals)
                    .diseaseName(request.getDiseaseName())
                    .diagnosisNotes(request.getDiagnosisNotes())
                    .labTestRecommendations(request.getLabTestRecommendations())
                    .doctorNotes(request.getDoctorNotes())
                    .followUpDate(request.getFollowUpDate())
                    .requiresReferral(Boolean.TRUE.equals(request.getRequiresReferral()))
                    .build();
            savedConsultation = consultationRepository.save(consultation);
            finalCode = generatedCode;
        }

        // Transition queue token status based on referral decision
        if (Boolean.TRUE.equals(request.getRequiresReferral())) {
            queueToken.setStatus(QueueStatus.REFERRED_TO_HOSPITAL);
        } else {
            queueToken.setStatus(QueueStatus.SENT_TO_PHARMACY);
        }
        queueTokenRepository.save(queueToken);

        // Record consultation event in patient medical history
        medicalHistoryService.recordEvent(
                patient,
                "CONSULTATION",
                String.format("Doctor Consultation: %s (%s)",
                        request.getDiseaseName() != null ? request.getDiseaseName() : "General Consultation",
                        finalCode),
                String.format("Diagnosis: %s. Lab Tests: %s. Referral required: %s.",
                        request.getDiagnosisNotes(),
                        request.getLabTestRecommendations() != null ? request.getLabTestRecommendations() : "None",
                        Boolean.TRUE.equals(request.getRequiresReferral()) ? "YES" : "NO"),
                finalCode,
                doctor.getFullName()
        );

        return consultationMapper.toResponse(savedConsultation);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationResponse getConsultationById(Long consultationId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Consultation", "id", consultationId));
        return consultationMapper.toResponse(consultation);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationResponse getConsultationByCode(String consultationCode) {
        Consultation consultation = consultationRepository.findByConsultationCodeAndIsDeletedFalse(consultationCode)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation", "consultationCode", consultationCode));
        return consultationMapper.toResponse(consultation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationResponse> getPatientConsultationHistory(Long patientId) {
        return consultationRepository.findByPatientIdAndIsDeletedFalseOrderByCreatedAtDesc(patientId)
                .stream()
                .map(consultationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationResponse> getDoctorConsultations(Long doctorId, Long campId) {
        return consultationRepository.findByDoctorIdAndCampIdAndIsDeletedFalseOrderByCreatedAtDesc(doctorId, campId)
                .stream()
                .map(consultationMapper::toResponse)
                .collect(Collectors.toList());
    }

    private synchronized String generateConsultationCode(Long campId) {
        int year = LocalDate.now().getYear();
        long count = consultationRepository.countByCampIdAndIsDeletedFalse(campId) + 1;
        String code = String.format("CNS-%d-%04d", year, count);
        while (consultationRepository.findByConsultationCodeAndIsDeletedFalse(code).isPresent()) {
            count++;
            code = String.format("CNS-%d-%04d", year, count);
        }
        return code;
    }
}
