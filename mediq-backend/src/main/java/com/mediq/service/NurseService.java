package com.mediq.service;

import com.mediq.dto.PatientVitalsResponse;
import com.mediq.dto.RecordVitalsRequest;

import java.util.List;

public interface NurseService {

    PatientVitalsResponse recordVitals(RecordVitalsRequest request);

    PatientVitalsResponse getVitalsById(Long vitalsId);

    PatientVitalsResponse getVitalsByTokenId(Long queueTokenId);

    List<PatientVitalsResponse> getPatientVitalsHistory(Long patientId);
}
