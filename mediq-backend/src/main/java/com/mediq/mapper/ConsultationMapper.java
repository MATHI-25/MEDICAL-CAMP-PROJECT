package com.mediq.mapper;

import com.mediq.dto.ConsultationResponse;
import com.mediq.entity.Consultation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConsultationMapper {

    private final PatientMapper patientMapper;
    private final UserMapper userMapper;
    private final VitalsMapper vitalsMapper;

    public ConsultationResponse toResponse(Consultation consultation) {
        if (consultation == null) {
            return null;
        }

        return ConsultationResponse.builder()
                .id(consultation.getId())
                .consultationCode(consultation.getConsultationCode())
                .patient(patientMapper.toResponse(consultation.getPatient()))
                .doctor(userMapper.toResponse(consultation.getDoctor()))
                .campId(consultation.getCamp() != null ? consultation.getCamp().getId() : null)
                .campTitle(consultation.getCamp() != null ? consultation.getCamp().getTitle() : null)
                .queueTokenId(consultation.getQueueToken() != null ? consultation.getQueueToken().getId() : null)
                .tokenNumber(consultation.getQueueToken() != null ? consultation.getQueueToken().getTokenNumber() : null)
                .vitals(vitalsMapper.toResponse(consultation.getVitals()))
                .diseaseName(consultation.getDiseaseName())
                .diagnosisNotes(consultation.getDiagnosisNotes())
                .labTestRecommendations(consultation.getLabTestRecommendations())
                .doctorNotes(consultation.getDoctorNotes())
                .followUpDate(consultation.getFollowUpDate())
                .requiresReferral(consultation.getRequiresReferral())
                .createdAt(consultation.getCreatedAt())
                .build();
    }
}
