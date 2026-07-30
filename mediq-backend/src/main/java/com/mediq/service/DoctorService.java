package com.mediq.service;

import com.mediq.dto.ConsultationResponse;
import com.mediq.dto.CreateConsultationRequest;

import java.util.List;

public interface DoctorService {

    ConsultationResponse saveConsultation(CreateConsultationRequest request);

    ConsultationResponse getConsultationById(Long consultationId);

    ConsultationResponse getConsultationByCode(String consultationCode);

    List<ConsultationResponse> getPatientConsultationHistory(Long patientId);

    List<ConsultationResponse> getDoctorConsultations(Long doctorId, Long campId);
}
