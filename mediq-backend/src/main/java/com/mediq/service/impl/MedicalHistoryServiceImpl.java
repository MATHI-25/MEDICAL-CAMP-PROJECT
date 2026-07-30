package com.mediq.service.impl;

import com.mediq.dto.MedicalHistoryResponse;
import com.mediq.entity.MedicalHistory;
import com.mediq.entity.Patient;
import com.mediq.repository.MedicalHistoryRepository;
import com.mediq.service.MedicalHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalHistoryServiceImpl implements MedicalHistoryService {

    private final MedicalHistoryRepository medicalHistoryRepository;

    @Override
    @Transactional
    public void recordEvent(Patient patient, String eventType, String eventSummary, String eventDetails, String referenceCode, String performedBy) {
        MedicalHistory history = MedicalHistory.builder()
                .patient(patient)
                .eventType(eventType)
                .eventSummary(eventSummary)
                .eventDetails(eventDetails)
                .referenceCode(referenceCode)
                .performedBy(performedBy)
                .eventTimestamp(LocalDateTime.now())
                .build();

        medicalHistoryRepository.save(history);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalHistoryResponse> getPatientTimeline(Long patientId) {
        return medicalHistoryRepository.findByPatientIdAndIsDeletedFalseOrderByEventTimestampDesc(patientId)
                .stream()
                .map(history -> MedicalHistoryResponse.builder()
                        .id(history.getId())
                        .patientId(history.getPatient().getId())
                        .eventType(history.getEventType())
                        .eventSummary(history.getEventSummary())
                        .eventDetails(history.getEventDetails())
                        .referenceCode(history.getReferenceCode())
                        .performedBy(history.getPerformedBy())
                        .eventTimestamp(history.getEventTimestamp())
                        .build())
                .collect(Collectors.toList());
    }
}
