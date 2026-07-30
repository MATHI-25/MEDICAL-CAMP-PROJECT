package com.mediq.service;

import com.mediq.dto.MedicalHistoryResponse;
import com.mediq.entity.Patient;

import java.util.List;

public interface MedicalHistoryService {

    void recordEvent(Patient patient, String eventType, String eventSummary, String eventDetails, String referenceCode, String performedBy);

    List<MedicalHistoryResponse> getPatientTimeline(Long patientId);
}
